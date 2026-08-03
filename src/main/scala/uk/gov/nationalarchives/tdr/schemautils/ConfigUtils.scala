package uk.gov.nationalarchives.tdr.schemautils

import scala.io.Source
import cats.data.Reader
import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.generic.auto._
import io.circe.jawn.decode
import ujson.Value.Value
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.nio.file.Paths

import scala.util.{Try, Using}

object ConfigUtils {

  val ARRAY_SPLIT_CHAR = ";"

  private val BASE_SCHEMA: String = "metadata-schema/baseSchema.schema.json"
  private val CONFIG_SCHEMA: String = "config-schema/config.json"

  private lazy val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)

  /** Loads the configuration files and returns a `MetadataConfiguration` object.
    *
    * This method uses the Reader monad to compose multiple functions that read from the configuration files. The resulting `MetadataConfiguration` object contains functions for:
    *   - Mapping alternate headers to property names.
    *   - Mapping property names to alternate headers.
    *   - Getting required columns for metadata downloads.
    *   - Getting property types.
    *   - Getting properties by property type.
    *   - Getting default values for properties.
    *
    * @return
    *   A `MetadataConfiguration` object containing the configuration mappings and functions.
    */
  def loadConfiguration: MetadataConfiguration = {
    val csvConfigurationReader = for {
      altHeaderToPropertyMapper <- Reader(inputToPropertyMapper)
      propertyToAltHeaderMapper <- Reader(propertyToOutputMapper)
      downloadFileOutputs <- Reader(getDownloadFilesOutputs)
      propertyType <- Reader(getPropertyType)
      getPropertiesByPropertyType <- Reader(getPropertiesByPropertyType)
      defaultValue <- Reader(getDefaultValue)
      propertyToDefaultValueMap <- Reader(getPropertiesToDefaultValueMap)
    } yield MetadataConfiguration(
      altHeaderToPropertyMapper,
      propertyToAltHeaderMapper,
      downloadFileOutputs,
      propertyType,
      getPropertiesByPropertyType,
      defaultValue,
      propertyToDefaultValueMap
    )
    csvConfigurationReader.run(configParameters)
  }

  /** This method takes configuration parameters and returns a curried function that maps a domain key to a property name.
    *
    * The returned function is curried, where the first parameter is the domain, and the second parameter is the key. It uses the configuration file to create a mapping of
    * alternate keys to property names.
    *
    * Prefer calling this via `MetadataConfiguration.inputToPropertyMapper(HeaderSource)` where possible to avoid string literals at call sites.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A curried function that takes two parameters: the domain and the key, and returns the corresponding property name.
    * @example
    *   - val metadataConfiguration = ConfigUtils.loadConfiguration
    *   - val tdrFileHeaderMapper = metadataConfiguration.inputToPropertyMapper(HeaderSource.TdrFileHeader)
    *   - tdrFileHeaderMapper("Date last modified") // Returns: "date_last_modified"
    */
  def inputToPropertyMapper(configurationParameters: ConfigParameters): String => String => String = {

    val configItems = getConfigItems(configurationParameters)
    val mapped = buildInputDomainMap(configItems)
    domain => key => mapped.get(domain).flatMap(_.get(key)).getOrElse(key)
  }

  /** This method takes configuration parameters and returns a function that maps a property name to an alternate key.
    *
    * The returned function is curried, where the first parameter is the domain, and the second parameter is the property name. It uses the configuration file to create a mapping
    * of property names to alternate keys.
    *
    * Prefer calling this via `MetadataConfiguration.propertyToOutputMapper(HeaderSource)` where possible to avoid string literals at call sites.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A curried function that takes two parameters: the domain and the property name, and returns the corresponding alternate key.
    * @example
    *   - val metadataConfiguration = ConfigUtils.loadConfiguration
    *   - val tdrPropertyMapper = metadataConfiguration.propertyToOutputMapper(HeaderSource.TdrFileHeader)
    *   - tdrPropertyMapper("date_last_modified") // Returns: "Date last modified"
    */
  def propertyToOutputMapper(configurationParameters: ConfigParameters): String => String => String = {

    val configItems = getConfigItems(configurationParameters)
    val altKeyMapped = buildOutputDomainMap(configItems)
    val mapped = altKeyMapped ++ Map(
      "expectedTDRHeader" -> configItems.map(cv => cv.key -> cv.expectedTDRHeader.toString).toMap,
      "allowExport" -> configItems.map(cv => cv.key -> cv.allowExport.toString).toMap,
      "fclExport" -> configItems.flatMap(cv => cv.fclExport.map(headerValue => cv.key -> headerValue)).toMap,
      "judgmentOnly" -> configItems.map(cv => cv.key -> cv.judgmentOnly.toString).toMap
    )
    domain => propertyName => mapped.get(domain).flatMap(_.get(propertyName)).getOrElse(propertyName)
  }

  /** This method takes configuration parameters and returns a function that retrieves the required columns for a given domain metadata download. It uses the configuration file to
    * create a mapping of domains to their required columns.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A function that takes a domain as a parameter and returns a list of DownloadFileDisplayProperties.
    * @example
    *   - val configParams = ConfigParameters(baseSchema, baseConfig)
    *   - val downloadFilesOutputs = getDownloadFilesOutputs(configParams)
    *   - downloadFilesOutputs("MetadataDownloadTemplate") // Returns: List(DownloadFileDisplayProperties("file_path", 1, false), ...)
    */
  private def getDownloadFilesOutputs(configurationParameters: ConfigParameters): String => List[DownloadFileDisplayProperty] = {
    val configItems: Map[String, List[DownloadFileDisplayProperty]] = configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .filter(_.downloadFilesOutputs.nonEmpty)
      .flatMap(item => item.downloadFilesOutputs.get.map(output => (output.domain, DownloadFileDisplayProperty(item.key, output.columnIndex, output.editable))))
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2))
      .toMap

    domain => configItems.getOrElse(domain, List.empty[DownloadFileDisplayProperty]).sortBy(_.columnIndex)
  }

  /** Retrieves the property type for a given property based on the configuration parameters.
    *
    * This method creates a mapping of property names to their types using the base schema from the configuration parameters. The resulting function takes a property name as input
    * and returns the corresponding property type as a string.
    *
    * @param configParameters
    *   The configuration parameters containing the base schema.
    * @example
    *   - val configParams = ConfigParameters(baseSchema, baseConfig)
    *   - val getType = getPropertyType(configParams)
    *   - getType("file_name") // Returns: "string"
    */
  private def getPropertyType(configParameters: ConfigParameters): String => String = {
    val propertyTypeMap = configParameters.baseSchema("properties").obj.foldLeft(Map.empty[String, String]) { case (acc, (propertyName, propertyValue)) =>
      val propertyType = propertyValue.obj.get("type") match {
        case Some(ujson.Str(singleType))              => singleType
        case Some(ujson.Arr(types)) if types.nonEmpty => types.collectFirst { case ujson.Str(value) if value != "null" => value }.getOrElse("unknown")
        case _                                        => "unknown"
      }
      acc + (propertyName -> propertyValue.obj.get("format").map(_.str).getOrElse(propertyType))

    }
    domain => propertyTypeMap.getOrElse(domain, "")
  }

  private def getPropertiesByPropertyType(configurationParameters: ConfigParameters): String => List[String] = {
    val configItems = configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
    propertyType => configItems.filter(_.propertyType == propertyType).map(_.key)
  }

  /** Retrieves the default value for a given property key based on the configuration parameters.
    *
    * @param configurationParameters
    *   The configuration parameters containing the config data.
    * @return
    *   A function that takes a key as a parameter and returns its default value if one exists.
    * @example
    *   - val configParams = ConfigParameters(baseSchema, baseConfig)
    *   - val getDefault = getDefaultValue(configParams)
    *   - getDefault("rights_copyright") // Returns: "Crown copyright"
    */
  private def getDefaultValue(configurationParameters: ConfigParameters): String => String = {
    val defaultValueMap = getPropertiesToDefaultValueMap(configurationParameters)

    key => defaultValueMap.getOrElse(key, "")
  }

  /** Retrieves a map of properties to default values
   *
   * @param configurationParameters
   *   The configuration parameters containing the config data.
   * @return
   *   mapping of between all properties with a default value and the default value itself
   *
   */
  private def getPropertiesToDefaultValueMap(configurationParameters: ConfigParameters): Map[String, String] = {
    configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .flatMap(item => item.defaultValue.map(defaultValue => item.key -> defaultValue))
      .toMap
  }

  private def buildInputDomainMap(configItems: Seq[ProcessedConfigItem]): Map[String, Map[String, String]] = {
    HeaderSource.values.map { source =>
      source.jsonFieldName -> configItems
        .flatMap(item => item.alternateKeyValues.get(source.jsonFieldName).map(headerValue => headerValue -> item.key))
        .toMap
    }.toMap
  }

  private def buildOutputDomainMap(configItems: Seq[ProcessedConfigItem]): Map[String, Map[String, String]] = {
    HeaderSource.values.map { source =>
      source.jsonFieldName -> configItems
        .flatMap(item => item.alternateKeyValues.get(source.jsonFieldName).map(headerValue => item.key -> headerValue))
        .toMap
    }.toMap
  }

  sealed trait HeaderSource {
    def jsonFieldName: String
    final def getFromAlternateKeys: AlternateKeys => Option[String] = _.values.get(jsonFieldName).filter(_.nonEmpty)
  }

  object HeaderSource {
    case object TdrFileHeader extends HeaderSource { val jsonFieldName = "tdrFileHeader" }
    case object TdrDataLoadHeader extends HeaderSource { val jsonFieldName = "tdrDataLoadHeader" }
    case object TdrBagitExportHeader extends HeaderSource { val jsonFieldName = "tdrBagitExportHeader" }
    case object SharePointTag extends HeaderSource { val jsonFieldName = "sharePointTag" }
    case object DroidHeader extends HeaderSource { val jsonFieldName = "droidHeader" }
    case object HardDriveHeader extends HeaderSource { val jsonFieldName = "hardDriveHeader" }
    case object NetworkDriveHeader extends HeaderSource { val jsonFieldName = "networkDriveHeader" }

    val values: List[HeaderSource] = List(
      TdrFileHeader, TdrDataLoadHeader, TdrBagitExportHeader,
      SharePointTag, DroidHeader, HardDriveHeader, NetworkDriveHeader
    )
  }

  private case class ProcessedConfigItem(
      key: String,
      alternateKeyValues: Map[String, String],
      expectedTDRHeader: Boolean,
      allowExport: Boolean,
      judgmentOnly: Boolean,
      fclExport: Option[String] = None
    )

  private def getConfigItems(configurationParameters: ConfigParameters): Seq[ProcessedConfigItem] = {
    configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .map(configVal => {
        val alternateKeysOpt = configVal.alternateKeys.headOption
        val altKeyMap: Map[String, String] = alternateKeysOpt.map { ak =>
          HeaderSource.values.flatMap(domain => domain.getFromAlternateKeys(ak).map(domain.jsonFieldName -> _)).toMap
        }.getOrElse(Map.empty)
        ProcessedConfigItem(
          key = configVal.key,
          alternateKeyValues = altKeyMap,
          expectedTDRHeader = configVal.expectedTDRHeader,
          allowExport = configVal.allowExport,
          judgmentOnly = configVal.judgmentOnly.contains(true),
          fclExport = alternateKeysOpt.flatMap(_.fclExport)
        )
      })
  }

  private def loadJsonResource(resourcePath: String): String = {
    val resourceFileName = mapToMetadataEnvironmentFile(resourcePath)
    val nodeSchema = Using(Source.fromResource(resourceFileName))(_.mkString)
    val mapper = new ObjectMapper()
    mapper.readTree(nodeSchema.get).toPrettyString
  }

  private def loadBaseSchema: Value = {
    val data = loadJsonResource(BASE_SCHEMA)
    ujson.read(data)
  }

  private def loadConfigFile: Either[io.circe.Error, Config] = {
    val data = loadJsonResource(CONFIG_SCHEMA)
    decode[Config](data)
  }

  case class MetadataConfiguration(
      inputToPropertyMapper: String => String => String,
      propertyToOutputMapper: String => String => String,
      downloadFileDisplayProperties: String => List[DownloadFileDisplayProperty],
      getPropertyType: String => String,
      getPropertiesByPropertyType: String => List[String],
      getDefaultValue: String => String,
      getPropertiesWithDefaultValue: Map[String, String]
    ) {
    /** Typed convenience overload for `inputToPropertyMapper`.
      *
      * @param source
      *   Header domain source.
      * @return
      *   A function mapping external/source header values to internal property keys.
      */
    def inputToPropertyMapper(source: HeaderSource): String => String = inputToPropertyMapper(source.jsonFieldName)

    /** Typed convenience overload for `propertyToOutputMapper`.
      *
      * @param source
      *   Header domain source.
      * @return
      *   A function mapping internal property keys to external/source header values.
      */
    def propertyToOutputMapper(source: HeaderSource): String => String = propertyToOutputMapper(source.jsonFieldName)
  }

  case class ConfigParameters(baseSchema: Value, baseConfig: Either[io.circe.Error, Config])

  case class DownloadFilesOutput(domain: String, columnIndex: Int, editable: Boolean)

  case class AlternateKeys(values: Map[String, String]) {
    def fclExport: Option[String] = values.get("fclExport").filter(_.nonEmpty)
  }

  case class ConfigItem(key: String, propertyType: String, expectedTDRHeader: Boolean, allowExport: Boolean,
                        alternateKeys: List[AlternateKeys], downloadFilesOutputs: Option[List[DownloadFilesOutput]],
                        defaultValue: Option[String] = None, judgmentOnly: Option[Boolean] = Option(false))

  private implicit val circeConfig: Configuration = Configuration.default.withDefaults
  implicit val alternateKeysDecoder: Decoder[AlternateKeys] = Decoder.instance { cursor =>
    cursor.value.asObject match {
      case Some(obj) =>
        val values = obj.toMap.flatMap { case (name, jsonValue) => jsonValue.asString.map(name -> _) }
        Right(AlternateKeys(values))
      case None => Left(io.circe.DecodingFailure("AlternateKeys must be a JSON object", cursor.history))
    }
  }
  implicit val configItemDecoder: Decoder[ConfigItem] = deriveConfiguredDecoder[ConfigItem]

  case class Config(configItems: List[ConfigItem])

  case class DownloadFileDisplayProperty(key: String, columnIndex: Int, editable: Boolean)

  def mapToMetadataEnvironmentFile(resourceName: String, metadataEnvironmentValue: Option[String] = sys.env.get("METADATA_VERSION_OVERRIDE")): String = {
    val startsWithSlash = resourceName.startsWith("/")
    val cleanResourceName = if (startsWithSlash) resourceName.substring(1) else resourceName

    metadataEnvironmentValue match {
      case Some(value) =>
        val path = Paths.get(cleanResourceName)
        val fileName = path.getFileName.toString
        val envSpecificName = cleanResourceName.replace(fileName, s"$value$fileName")

        Try(Source.fromResource(envSpecificName)).toOption match {
          case Some(source) =>
            source.close()
            if (startsWithSlash) s"/$envSpecificName" else envSpecificName
          case None =>
            if (startsWithSlash) s"/$cleanResourceName" else cleanResourceName
        }
      case None => if (startsWithSlash) s"/$cleanResourceName" else cleanResourceName
    }
  }

}

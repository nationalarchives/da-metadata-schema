package uk.gov.nationalarchives.tdr.schemautils

import scala.io.Source
import cats.data.Reader
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.circe.generic.auto.*
import io.circe.jawn.decode
import ujson.Value.Value

import java.io.InputStream
import scala.util.Using

object ConfigUtils {

  val ARRAY_SPLIT_CHAR = ";"

  private val BASE_SCHEMA: String = "/metadata-schema/baseSchema.schema.json"
  private val CONFIG_SCHEMA: String = "config-schema/config.json"

  private lazy val configParameters: ConfigParameters = ConfigParameters(loadBaseSchema, loadConfigFile)

  /** Loads the configuration files and returns a `MetadataConfiguration` object.
    *
    * This method uses the Reader monad to compose multiple functions that read from the configuration files. The resulting `MetadataConfiguration` object contains functions for:
    *   - Mapping alternate headers to property names.
    *   - Mapping property names to alternate headers.
    *   - Getting required columns for metadata downloads.
    *   - Getting property types.
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
    } yield MetadataConfiguration(altHeaderToPropertyMapper, propertyToAltHeaderMapper, downloadFileOutputs, propertyType)
    csvConfigurationReader.run(configParameters)
  }

  /** This method takes configuration parameters and returns a curried function that maps a domain key to a property name.
    *
    * The returned function is curried, where the first parameter is the domain, and the second parameter is the key. It uses the configuration file to create a mapping of
    * alternate keys to property names.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A curried function that takes two parameters: the domain and the key, and returns the corresponding property name.
    * @example
    *   - val configParams = ConfigParameters(baseSchema, baseConfig)
    *   - val inputMapper = inputToPropertyMapper(configParams)
    *   - val tdrFileHeaderMapper = inputMapper("tdrFileHeader")
    *   - tdrFileHeaderMapper("Date last modified") // Returns: "date_last_modified"
    */
  def inputToPropertyMapper(configurationParameters: ConfigParameters): String => String => String = {

    val configItems = getConfigItems(configurationParameters)
    val mapped =
      Map(
        "tdrFileHeader" -> configItems.filter(_._2.nonEmpty).map(p => p._2.get -> p._1).toMap,
        "tdrDataLoadHeader" -> configItems.filter(_._3.nonEmpty).map(p => p._3 -> p._1).toMap,
      )
    domain => key => mapped.get(domain).flatMap(_.get(key)).getOrElse(key)
  }

  /** This method takes configuration parameters and returns a function that maps a property name to an alternate key.
    *
    * The returned function is curried, where the first parameter is the domain, and the second parameter is the property name. It uses the configuration file to create a mapping
    * of property names to alternate keys.
    *
    * @param configurationParameters
    *   The configuration parameters containing the base schema and configuration data.
    * @return
    *   A curried function that takes two parameters: the domain and the property name, and returns the corresponding alternate key.
    * @example
    *   - val configParams = ConfigParameters(baseSchema, baseConfig)
    *   - val propertyMapper = propertyToOutputMapper(configParams)
    *   - val tdrPropertyFileHeaderMapper("tdrFileHeader")
    *   - tdrPropertyFileHeaderMapper("date_last_modified") // Returns: "Date last modified"
    */
  def propertyToOutputMapper(configurationParameters: ConfigParameters): String => String => String = {

    val configItems = getConfigItems(configurationParameters)
    val mapped =
      Map(
        "tdrFileHeader" -> configItems.filter(_._2.nonEmpty).map(p => p._1 -> p._2.get).toMap,
        "tdrDataLoadHeader" -> configItems.filter(_._3.nonEmpty).map(p => p._1 -> p._3).toMap,
        "expectedTDRHeader" -> configItems.map(p => p._1 -> p._4.toString).toMap
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

  private def getConfigItems(configurationParameters: ConfigParameters) = {
    configurationParameters.baseConfig
      .getOrElse(Config(List.empty[ConfigItem]))
      .configItems
      .map(p => (p.key, p.alternateKeys.head.tdrFileHeader, p.alternateKeys.head.tdrDataLoadHeader, p.expectedTDRHeader))
  }

  private def loadBaseSchema: Value = {
    val nodeSchema: JsonNode = getJsonNodeFromStreamContent(getClass.getResourceAsStream(BASE_SCHEMA))
    ujson.read(nodeSchema.toPrettyString)
  }

  private def getJsonNodeFromStreamContent(content: InputStream): JsonNode = {
    val mapper = new ObjectMapper()
    mapper.readTree(content)
  }

  private def loadConfigFile: Either[io.circe.Error, Config] = {
    val nodeSchema = Using(Source.fromResource(CONFIG_SCHEMA))(_.mkString)
    val mapper = new ObjectMapper()
    val data = mapper.readTree(nodeSchema.get).toPrettyString
    decode[Config](data)
  }

  case class MetadataConfiguration(
      inputToPropertyMapper: String => String => String,
      propertyToOutputMapper: String => String => String,
      downloadFileDisplayProperties: String => List[DownloadFileDisplayProperty],
      getPropertyType: String => String
  )

  case class ConfigParameters(baseSchema: Value, baseConfig: Either[io.circe.Error, Config])

  case class DownloadFilesOutput(domain: String, columnIndex: Int, editable: Boolean)

  case class AlternateKeys(tdrFileHeader: Option[String], tdrDataLoadHeader: String)

  case class ConfigItem(key: String, propertyType: String, expectedTDRHeader: Boolean, alternateKeys: List[AlternateKeys], downloadFilesOutputs: Option[List[DownloadFilesOutput]])

  case class Config(configItems: List[ConfigItem])

  case class DownloadFileDisplayProperty(key: String, columnIndex: Int, editable: Boolean)

}

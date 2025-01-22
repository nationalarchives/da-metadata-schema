# Digital Archiving Metadata JSON Schema

This project provides the [JSON schemas](https://json-schema.org/) for defining [metadata fields](https://www.nationalarchives.gov.uk/information-management/manage-information/digital-records-transfer/what-are-born-digital-records/) and their requirements for The National Archives. It aims to standardize the structure of metadata across teams/catalogues, facilitating interoperability and consistency in data representation.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Schema](#schemas)
4. [Validation Messages](#validation-messages)
5. [Usage](#usage)

## Introduction

The National Archives (TNA) collects and preserves digital records from various sources, including government departments, courts and public enquiries. To manage these records effectively, TNA requires a consistent and structured approach to metadata creation and management.  
While JSON is probably the most popular format for exchanging data, JSON Schema is the vocabulary that enables JSON data consistency, validity, and interoperability at scale.  
This project provides a set of JSON schemas that define the structure and requirements of metadata fields used in TNA Digital Archiving. The schema are designed to conform to the JSON Schema specification, extended to include custom keywords and properties specific to TNA Digital Archiving metadata requirements.

## Features

- **Flexibility**: The schema supports a wide range of metadata fields commonly found in catalogues, including textual descriptions, numerical values, dates, and more.
- **Validation**: Ensures that metadata entries adhere to a predefined structure and meet specified requirements, reducing errors and inconsistencies.
- **Extensibility**: Easily extend the schema to accommodate additional metadata fields or custom requirements specific to different teams or use cases.

## JSON Schema

JSON Schema have [defined keywords](https://json-schema.org/understanding-json-schema/reference) used to define data.

* The National Archives schema are based on the [JSON schema draft-07](https://json-schema.org/specification-links.html#draft-7) specification 
with extensions for domain specific requirements, including:
- `daBeforeToday` indicates a supplied date must be before now
- `alternateKeys` allows for alternate names for metadata fields

## Schemas

### Configuration schema
Metadata standards are defined against specific field names. Each team may use alternate names for these fields but still require the same validation. The mapping is defined in `alternateKeys`.  
CSV files are the standard way to upload metadata to TDR and their data value `type` needs to be evaluated.
* CSV file headers are defined in `alternateKeys -> tdrFileHeader`.  
* conversions from the CSV string to the `type` required for validation.

The [base schema](#base-schema) is used for the configurations

### Validation schemas used to define the metadata

These schemas are used by the [JSON schema validator](https://github.com/networknt/json-schema-validator) to validate metadata.
1. [base schema](#base-schema)
2. [definitions schema](#definitions-schema)
3. [closure schema open](#closure-schema-open)
4. [closure schema closed](#closure-schema-open)
5. [relationship schema](metadata-schema/relationshipSchema.schema.json)
6. [required schema](metadata-schema/requiredSchema.schema.json)  


#### Additional schemas are used to define specific use cases for metadata
* [data load SharePoint schema](metadata-schema/dataLoadSharePointSchema.schema.json)

#### Base Schema
The [base schema](metadata-schema/baseSchema.schema.json) defines the metadata fields that are supported within Digital Archiving.
* Example definition for `end_date`
```json
 {
  "end_date": {
    "description": "The date the record ends",
    "type": [
      "string",
      "null"
    ],
    "format": "date",
    "propertyType": "Supplied",
    "alternateKeys": [
      {
        "tdrFileHeader": "Date of the record",
        "tdrDataLoadHeader": "end_date"
      }
    ],
    "daBeforeToday": "Validates that end date is earlier than today's date"
  }
}
```
* end_date field key
* type - value can be a string or null (undefined)
* format - date - the string will be in the format 2018-11-13 (This date format is specific for TRD)
* alternateKeys -> tdrFileHeader - Date of the record  (The column header in the TDR metadata file)
* daBeforeToday - the date must be before today

#### Definitions Schema
The [definitions schema](metadata-schema/definitionsSchema.schema.json) defines allowed values for fields (such as FOI exemption codes). These can then be referenced in other schemas.

#### Closure Schema Open
The [closure schema open](metadata-schema/closureSchemaOpen.schema.json) defines the schema for `Open` records  
* if closure_type is Open
  * Then
    * no closure_period 
    * no closure_start_date
    * no description_closed
    * no foi_exemption_asserted
    * no foi_exemption_code
    * no title_alternate
    * no description_alternate
    * title_closed is false
    * description_closed is false

#### Closure Schema Closed
The [closure schema closed](metadata-schema/closureSchemaClosed.schema.json) defines the schema for `Closed` records
* if closure_type is Closed
  * Then the following fields are required
    * closure_period
    * closure_start_date
    * description_closed
    * foi_exemption_asserted
    * foi_exemption_code
    * title_alternate
    * description_alternate
    * title_closed 
    * description_closed 
  * If title_closed is true then title_alternate is required
  * If description_closed is true then description_alternate is required

#### Relationship Schema

This schema is used to enforce cross attribute relationships.
```json
 {
      "if": {
        "properties": {
          "file_name_translation": {
            "type": "string",
            "minLength": 1
          }
        }
      },
      "then": {
        "properties": {
          "file_name_translation_language": {
            "type": "string",
            "minLength": 1
          }
        },
        "required": ["file_name_translation_language"]
      }
    }
```

If there is a `file_name_translation` then there must be a `file_name_translation_language`.

#### Required Schema
The [required schema](metadata-schema/requiredSchema.schema.json) defines the required fields in a metadata file uploaded to TDR
```json
{
  "$id": "/schema/required",
  "type": "object",
  "required": [
    "file_path",
    "end_date",
    "description",
    "closure_type",
    "closure_period",
    "closure_start_date",
    "description_closed",
    "foi_exemption_asserted",
    "foi_exemption_code",
    "title_closed",
    "title_alternate",
    "description_alternate"
  ]
}
```

#### Data Load SharePoint Schema

The [data load SharePoint schema](metadata-schema/dataLoadSharePointSchema.schema.json) defines what properties are permitted when loading metadata directly from SharePoint. 

It is a sub-set of the [Base Schema](metadata-schema/baseSchema.schema.json) properties.

Example data:
```json
{
  "date_last_modified": "2001-12-12",
  "client_side_checksum": "8b9118183f01b3df0fc5073feb68f0ecd5a7f85a88ed63ac7d0d242dc2aba2ea",
  "file_size": 26,
  "file_path": "a/filepath/filename1.docx",
  "UUID": "b8b624e4-ec68-4e08-b5db-dfdc9ec84fea"
}
```

### Error file schema
The output of a metadata file validation should be a JSON file conforming to the [error file schema](errors/errorFileSchema.json)  
An example of an error file conforming to this schema.
```json
{
  "consignmentId" : "5049c395-6124-40fd-bffa-3fe44223bbd0",
  "date" : "2025-01-22",
  "fileError" : "SCHEMA_VALIDATION",
  "validationErrors" : [
    {
      "assetId" : "test/hi.txt",
      "errors" : [
        {
          "validationProcess" : "SCHEMA_CLOSURE_CLOSED",
          "property" : "Closure Start Date",
          "errorKey" : "type",
          "message" : "Must be provided for a closed record"
        }
        
      ],
      "data" : [
        {
          "name" : "Closure Start Date",
          "value" : ""
        }
      ]
    }
  ]
}
```
* `consignmentId` is the unique identifier for the consignment
* `date` is the date the error file was created
* `fileError` is the type of error. SCHEMA_VALIDATION indicates a schema validation error
* `assetId` for TDR is the file path as this should be unique and the error can be tracked back to the original
* `validationProcess` indicates the schema used for validation that produced the error
* `errorKey` is the keyword returned from the Json Schema validation. 'type' indicated null and not a String
* `property` is the input key (TDR metadata file column header)
* `message` is the user-friendly message for the error obtained as below, using the property name as used in validation

### Validation Messages

The [tdr-metadata-validation](https://github.com/nationalarchives/tdr-metadata-validation) produces errors with the following properties:
* ```validationProcess```
* ```property```
* ```errorKey```

For user-friendly messages see [Validation-message.properties file](validation-messages/validation-messages.properties)  
The format is {validationProcess}.{property}.{errorKey}={User friendly message}

## Usage

To use the JSON schema in your project, follow these steps:

1. **Download**: Clone this repository or download the `*schema.json` files directly.
2. **Integration**: Integrate the `*schema.json` files into your project where metadata validation is required.
3. **Validation**: Use JSON schema validation libraries in your preferred programming language to validate metadata objects against the provided schema.

An example using scala and the [networknt json-schema-validator library](https://github.com/networknt/json-schema-validator) is shown in [BaseSpec.scala](src/test/scala/uk/gov/tna/metadata/schema/validator/BaseSpec.scala).

```scala
      // load schema
      val schemaPath = "metadata-schema/relationshipSchema.schema.json"
      val schemaInputStream: InputStream = Files.newInputStream(new File(schemaPath).toPath)
      val schema = getJsonSchemaFromStreamContentV7(schemaInputStream)
      
      // load data
      val dataPath = "/data/relationship.json"
      val dataInputStream = getClass.getResourceAsStream(dataPath)
      val node = getJsonNodeFromStreamContent(dataInputStream)

      // validate data
      val errors: util.Set[ValidationMessage] = schema.validate(node.toPrettyString, InputFormat.JSON)
```

## Local development

To publish the schemas locally, run the following commands from the repository directory:

```
  $ sbt copySchema copyValidationMessageProperties package publishLocal

```

Other sbt projects that have this project as a dependency can access the local snapshot version by changing the version number in their build.sbt or dependencies file, for example:
  ```
  ... other dependencies...
  "uk.gov.nationalarchives" % "da-metadata-schema_3" % "[version number]-SNAPSHOT"
  ... other dependences...
```

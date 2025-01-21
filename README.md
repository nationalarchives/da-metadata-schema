# Digital Archiving Metadata JSON Schema

This project provides the [JSON schemas](https://json-schema.org/) for defining [metadata fields](https://www.nationalarchives.gov.uk/information-management/manage-information/digital-records-transfer/what-are-born-digital-records/) and their requirements for The National Archives. It aims to standardize the structure of metadata across catalogues, facilitating interoperability and consistency in data representation.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Schema](#schemas)
4. [Validation Messages](#validation-messages)
5. [Usage](#usage)

## Introduction

Catalogues often contain diverse types of data, and consistent metadata structures are crucial for effective data management and searching. This JSON schema defines a standardised format for describing metadata fields, their types, and any constraints or requirements associated with each field.

## Features

- **Flexibility**: The schema supports a wide range of metadata fields commonly found in catalogues, including textual descriptions, numerical values, dates, and more.
- **Validation**: Ensures that metadata entries adhere to a predefined structure and meet specified requirements, reducing errors and inconsistencies.
- **Extensibility**: Easily extend the schema to accommodate additional metadata fields or custom requirements specific to different catalogues or use cases.

## JSON Schema

JSON Schema have [defined keywords](https://json-schema.org/understanding-json-schema/reference) used to define data.


The National Archives use the defined keywords and extensions for domain specific requirements. These include
- `daBeforeToday` indicates a supplied date must be before now

## Schemas

### Error file schema
The output of a metadata file validation should be a JSON file conforming to this schema.  
* [error file schema](errors/errorFileSchema.json)

### Mapping configuration schema
The base schema defines metadata fields that are allowed in TNA Digital Archiving. Different teams can use alternate names for the fields described in ```alternateKeys```  
* [base schema](#base-schema)
1. tdrFileHeader - the value used in the TDR metadata csv upload file 
2. type - the type of the field. This is used to allow conversions between the CSV string value and the type for validation

### Validation schemas used to define the metadata

These schemas are used by the [JSON schema validator](https://github.com/networknt/json-schema-validator) to validate the metadata.
1. [base schema](#base-schema)
2. [closure schema open](#closure-schema-open)
3. [closure schema closed](#closure-schema-open)
4. [relationship schema](metadata-schema/relationshipSchema.schema.json)
5. [required schema](metadata-schema/requiredSchema.schema.json)  

The schema are based on the [JSON schema draft-07](https://json-schema.org/specification-links.html#draft-7) specification. TNA Digital Archiving have custom extensions.
* daBeforeToday (type) - the date must be before today (The schema validator can be extended to support this)
* alternateKeys - alternate names for the field key

### Additional schemas are used to define specific use cases for metadata
* [data load SharePoint schema](metadata-schema/dataLoadSharePointSchema.schema.json)

### Base Schema
Definition for a ```UUID```
* UUID - field key 
* type - the value must be a string
* format - the string format must be an uuid
* tdrFileHeader - the human-readable key 

Definition for ```date_last_modified```
* date_last_modified field key
* type - value can be a string or null
* format - date - the string will be in the format 2023-11-13 (YYYY-MM-DD)
* tdrFileHeader - alternate name used for key ClientSideFileLastModifiedDate

Definition for ```end_date```
* end_date field key
* type - value can be a string or null
* format - date - the string will be in the format 2018-11-13
* tdrFileHeader - alternate name used for key ClientSideFileLastModifiedDate

Example data
```
{
  "UUID": "f373d856-d4ee-4b41-ae89-d7327915c73e",
  "file_path":"file:///C:/atransfer/content/interesting.pdf",
  "foi_exemption_code":[],
  "date_last_modified": "2001-12-03",
  "description": "description for catalogue"
}
```

### Closure Schema Open
The [closure schema open](metadata-schema/closureSchemaOpen.schema.json) defines the schema for ```Open``` records  
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
    

### Relationship Schema

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

### Required Schema
This schema defines the required fields in a metadata file uploaded to TDR

### Data Load SharePoint Schema

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
## Validation Messages

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

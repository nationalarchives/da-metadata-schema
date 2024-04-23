# Catalogue Metadata JSON Schema

This project provides the JSON schemas for defining metadata fields and their requirements for The National Archives. It aims to standardize the structure of metadata across for catalogues, facilitating interoperability and consistency in data representation.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Schema](#schmeas)
3. [Usage](#usage)
4. [Contributing](#contributing)
5. [License](#license)

## Introduction

Catalogues often contain diverse types of data, and consistent metadata structures are crucial for effective data management and searchability. This JSON schemas define a standardized format for describing metadata fields, their types, and any constraints or requirements associated with each field.

## Features

- **Flexibility**: The schema supports a wide range of metadata fields commonly found in catalogues, including textual descriptions, numerical values, dates, and more.
- **Validation**: Ensures that metadata entries adhere to a predefined structure and meet specified requirements, reducing errors and inconsistencies.
- **Extensibility**: Easily extend the schema to accommodate additional metadata fields or custom requirements specific to different catalogues or use cases.

## Schemas

Three schemas are used to define the metadata
1. [base schema](#base-schema)
2. [closure schema](#closure-schema)
3. [relationship schema](metadata-schema/relationshipSchema.schema.json)

### Base Schema
The [base schema](metadata-schema/baseSchema.schema.json) defines the supported fields (names) and value types.
```angular2html
.....
"UUID": {
      "type": "string",
      "format": "uuid",
      "tdrDescription": "ID value for the record",
      "message": {
        "format": "uuid must be a valid UUID"
      },
      "tdrName": "UUID"
    },
 "date_last_modified": {
     "type": [
        "string",
        "null"
     ],
     "pattern": "^(0?[1-9]|[12][0-9]|3[01])[\\/\\-](0?[1-9]|1[012])[\\/\\-]\\d{4}$|\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d(?:\\.\\d+)?Z?",
     "tdrName": "ClientSideFileLastModifiedDate"
 }
.....
```
Definition for a UUID
* UUID - field key 
* type - the value must be a string
* format - the string format must be a uuid
* tdrDescription - the human-readable key 
* tdrName - alternate name for the key
* message - the message used in error reporting when validating data

Definition for date_last_modified
* date_last_modified field key
* type - value can be a string or null
* pattern - the string will be in the format dd/mm/yyyy or yyyy-mm-ddThh:mm:ss
* tdrName - alternate name used for key ClientSideFileLastModifiedDate

Example data
```angular2html
{
  "UUID": "f373d856-d4ee-4b41-ae89-d7327915c73e",
  "file_path":"file:///E:/DADRI_001/content/windsor.pdf",
  "foi_exemption_code":[],
  "date_last_modified": "12/12/2001",
  "description": "description for catalogue"
}
```
### Closure Schema
The [closure schema](metadata-schema/closureSchema.schema.json) defines the schema for closure information. The schema enforces the presence of several fields and their values that are dependant upon other field values.  
```
"allOf": [
    {
      "if": {
        "properties": {
          "closure_type": {
            "pattern": "open_on_transfer|OPEN"
          }
        }
      },
      "then": {
        "properties": {
          "closure_period": {
            "type": [
              "number",
              "null"
            ],
            "maximum": 0
          },
          "closure_start_date": {
            "type": [
              "string",
              "null"
            ],
            "maxLength": 0
          },
          ......
        },
        "else": {
          "properties": {
            "foi_exemption_asserted": {
              "type": "string",
              "pattern": "^(0?[1-9]|[12][0-9]|3[01])[\\/\\-](0?[1-9]|1[012])[\\/\\-]\\d{4}$|\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d(?:\\.\\d+)?Z?"
            },
            "closure_type": {
              "enum": [
                "closed_review",
                "closed_for",
                "CLOSED"
            ],
            ......
           },
         },
         "required": [
           "closure_period",
           "closure_start_date",
           "description_closed",
           "foi_exemption_asserted",
           "foi_exemption_code"
        ]
```

Multiple if/then/else statements allowed
* if closure_type OPEN
  * Then
    * closure_period must not be set or 0
    * no closure_start_date
    * ....
  * else
    * there must be a foi_exemption_asserted date
    * the closure_type must be one of closed_review, closed_for or CLOSED
    * ...
    * there must be values for closure_period, closure_start_date, description_closed, foi_exemption_asserted, foi_exemption_code 
### Relationship Schema

This schema is used to enforce cross attribute relationships

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

If there is a file_name_translation then there must be a file_name_translation 

## Usage

To use the JSON schema in your project, follow these steps:

1. **Download**: Clone this repository or download the `*schema.json` files directly.
2. **Integration**: Integrate the `*schema.json` files into your project where metadata validation is required.
3. **Validation**: Use JSON schema validation libraries in your preferred programming language to validate metadata objects against the provided schema.

Example using scala and the [networknt json-schema-validator library](https://github.com/networknt/json-schema-validator):

Is shown in [SchemaDataTypeSpec.scala](src/test/scala/uk/gov/tna/tdr/metadata/schema/validator/SchemaDataTypeSpec.scala)

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

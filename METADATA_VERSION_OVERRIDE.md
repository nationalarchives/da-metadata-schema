# Testing Unreleased Metadata Changes with `METADATA_VERSION_OVERRIDE`

## Overview

Use `METADATA_VERSION_OVERRIDE` when you want to introduce or test a new metadata field without changing the default schemas used by released environments.

The override works by looking for environment-specific files with a prefix. For example, if `METADATA_VERSION_OVERRIDE` is set to `TDRD-1475-`, the TDR application will try to load prefixed files such as:

- `config-schema/TDRD-1475-config.json`
- `metadata-schema/TDRD-1475-baseSchema.schema.json`
- `metadata-schema/TDRD-1475-requiredSchema.schema.json`

If a prefixed file does not exist, the default file is used instead.

When a prefixed file does exist, it is loaded instead of the default file. The files are not merged together, so an override file should normally be created by copying the current default file and then editing it.

This lets you test changes safely in downstream services before merging them into the standard metadata version.

## When to use this

Use this approach when you:

- need to add a new field for a feature
- want to test the new field in a specific environment only
- do not want the change to appear in all environments or releases yet

## How the override naming works

Choose a prefix that matches your feature or Jira ticket, for example:

- `TDRD-1475-`
- `note-`

The trailing hyphen is important because the override is applied as a filename prefix.

For example:

- default file: `config-schema/config.json`
- override file: `config-schema/TDRD-1475-config.json`


## Steps to add a new field behind an override

### 1. Create an override config file

Create a new file in `config-schema/` using the feature name or Jira ticket id.

Start by copying `config-schema/config.json` to the new prefixed filename, then update the copied file with your new field configuration.

Example:

- `config-schema/TDRD-1475-config.json`

Add the configuration for the new field there. In most cases this means adding a new `configItems` entry with the field `key`, `propertyType`, `alternateKeys`, and any download/export settings required by downstream services.

A typical config entry will reference the base schema definition for the field:

```json
{
  "key": "new_field",
  "$ref": "classpath:/metadata-schema/baseSchema.schema.json#/properties/new_field",
  "propertyType": "Supplied",
  "expectedTDRHeader": true,
  "allowExport": true,
  "alternateKeys": [
    {
      "tdrFileHeader": "new field",
      "tdrDataLoadHeader": "NewField"
    }
  ],
  "downloadFilesOutputs": []
}
```

> If you are testing through an override, make sure the config file references the schema definition that exists for that override flow.

### 2. Create the relevant override schema files

Create prefixed files in `metadata-schema/` for each schema that needs to change.

As with the config file, start by copying the existing default schema file and then apply your changes to the copied version.

Common examples are:

- `metadata-schema/TDRD-1475-baseSchema.schema.json`
- `metadata-schema/TDRD-1475-requiredSchema.schema.json`

Only create override files for the schemas that need different behaviour. Any file you do not override will continue to use the default version.

## Enable the override in `tdr-terraform-environment`

Set `metadata_version_override` in the environment where you want to test the new field.

Example:

```hcl
metadata_version_override = local.environment == "intg" ? "TDRD-1475-" : ""
```

This causes downstream services in that environment to use the prefixed metadata files when they are available.

## Apply the change to the target environment

After updating `tdr-terraform-environment`, apply the Terraform change to the environment where you want the new field to appear.

Once applied, the new field should appear in metadata download templates, provided the override config and schemas include the field.

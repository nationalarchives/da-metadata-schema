# Excluded Filenames

## Overview

The `excluded-filenames.json` configuration file defines filename patterns that should be filtered out before uploading files to the database. This allows the system to automatically exclude system files and temporary files that users don't typically want to upload.

## Configuration File

Location: `puids/excluded-filenames.json`

The JSON file contains an array of filename pattern objects, each with the following properties:

- **pattern** (string, required): The pattern to match against filenames
  - For "exact" type: The exact filename to match
  - For "regex" type: A regular expression pattern

- **type** (string, required): The type of pattern matching
  - `"exact"`: Exact string matching
  - `"regex"`: Regular expression matching

- **caseInsensitive** (boolean, required): Whether the matching should be case-insensitive
  - `true`: Match regardless of case (e.g., `thumbs.db` matches `Thumbs.db`, `THUMBS.DB`)
  - `false`: Case-sensitive matching

- **description** (string, required): A human-readable description of what this pattern excludes

## Current Exclusions

The following files are currently excluded by default:

1. **thumbs.db** (exact, case-insensitive)
   - Windows thumbnail cache file
   - Matches: `thumbs.db`, `Thumbs.db`, `THUMBS.DB`

2. **desktop.ini** (exact, case-sensitive)
   - Windows desktop configuration file
   - Matches only: `desktop.ini`

3. **.DS_Store** (exact, case-sensitive)
   - macOS folder metadata file
   - Matches only: `.DS_Store`

## Usage in Code

The plugin automatically generates a Scala object `ExcludedFilenames`:

```scala
import uk.gov.nationalarchives.tdr.schema.generated.ExcludedFilenames

// Check if a single filename should be excluded
val shouldExclude = ExcludedFilenames.isExcluded("thumbs.db") // true

// Filter a list of filenames
val filenames = Seq("document.pdf", "thumbs.db", "Desktop.ini", "desktop.ini")
val filtered = filenames.filterNot(ExcludedFilenames.isExcluded)
// Returns: Seq("document.pdf", "Desktop.ini")
```

## Adding New Exclusions

### Example 1: Exclude files starting with tilde (~)

Add to `excluded-filenames.json`:

```json
{
  "pattern": "^~.*",
  "type": "regex",
  "caseInsensitive": false,
  "description": "Temporary files starting with tilde"
}
```

### Example 2: Exclude exact filename

Add to `excluded-filenames.json`:

```json
{
  "pattern": "temporary.tmp",
  "type": "exact",
  "caseInsensitive": true,
  "description": "Temporary file"
}
```

### Example 3: Exclude files with specific extension

Add to `excluded-filenames.json`:

```json
{
  "pattern": "^.*\\.tmp$",
  "type": "regex",
  "caseInsensitive": true,
  "description": "All temporary files with .tmp extension"
}
```

## Regex Pattern Tips

Regex matching is still supported even though current defaults use exact matching.

Examples:
- `^~.*` - Files starting with `~`
- `^.*\\.bak$` - Files ending with `.bak`
- `^(thumbs|desktop)\\..*` - Files starting with `thumbs.` or `desktop.`

## Plugin Details

The `ExcludedFilenamesGeneratorPlugin` automatically:

1. Reads the `puids/excluded-filenames.json` configuration file
2. Generates a Scala object at compile time with:
   - Case class definitions for pattern matching
   - Matching support for both `exact` and `regex` types
   - Efficient regex compilation and caching
3. Includes the generated code in `target/scala-2.13.16/src_managed/main/generated/ExcludedFilenames.scala`

## Relation to Disallowed PUIDs

Previously, some system files like `desktop.ini` (PUID: x-fmt/421) were handled via the disallowed PUIDs list. The filename exclusion approach is more appropriate for system files because:

1. It filters files before they're sent to the database
2. It's based on filename rather than PRONOM format identification
3. It's faster as it doesn't require file format identification
4. It's more maintainable for system-specific files

The PUID `x-fmt/421` has been removed from the disallowed PUIDs list as it's now handled by filename exclusion.

## Testing

Comprehensive tests are available in:
`src/test/scala/uk/gov/nationalarchives/tdr/schema/generated/ExcludedFilenamesSpec.scala`

Run tests with:
```bash
sbt test
```

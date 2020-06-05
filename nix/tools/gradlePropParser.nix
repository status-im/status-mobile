# Parser for android/gradle.properties file.
# Returns an attrset with keys and values from it.
{ lib }:

gradlePropsFile:

let
  inherit (lib) head last filter listToAttrs splitString nameValuePair hasPrefix readFile;

  # Read lines
  lines = splitString "\n" (readFile gradlePropsFile);

  isKeyValueLine = line: line != "" && !hasPrefix "#" line;
  cleanup = lines: filter isKeyValueLine lines;
  extractKeyValues = line:
    let flag = splitString "=" line;
    in nameValuePair (head flag) (last flag);
  parseAttrs = lines: listToAttrs (map extractKeyValues lines);
in
  parseAttrs (cleanup lines)

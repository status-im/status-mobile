{ lib }:

version:
let
  # paths don't like slashes in them
  dropSlashes = builtins.replaceStrings [ "/" ] [ "_" ];
  # if version doesn't match this it's probably a commit, it's lax semver
  versionRegex = "^v?[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+[[:alnum:]+_.-]*$";
in
  if (builtins.match versionRegex version) != null
  # Geth forces a 'v' prefix for all versions
  then lib.removePrefix "v" (dropSlashes version)
  # reduce metrics cardinality in Prometheus
  else lib.traceValFn (_: "WARNING: Marking build version as 'develop'!") "develop"

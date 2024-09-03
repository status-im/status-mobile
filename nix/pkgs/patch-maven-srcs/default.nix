# This script patches build.gradle files in node_modules to use
# our local version of Maven dependencies instead of fetching them.

{ stdenv, writeScript, runtimeShell }:

writeScript "patch-maven-srcs" (''
  #!${runtimeShell}
  # Source setup.sh for substituteInPlace
  source ${stdenv}/setup

  function patchMavenSource() {
    if [ "$IN_NIX_BUILD_DERIVATION" = "TRUE" ]; then
      sed -i '/repositories {/a \    mavenLocal()' "$1"
    fi
  }

  gradleFile="$1"

  patchMavenSource "$gradleFile"
'')

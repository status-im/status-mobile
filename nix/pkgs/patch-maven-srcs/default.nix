# This script patches build.gradle files in node_modules to use
# our local version of Maven dependencies instead of fetching them.

{ stdenv, writeScript, runtimeShell }:

writeScript "patch-maven-srcs" (''
  #!${runtimeShell}
  # Source setup.sh for substituteInPlace
  source ${stdenv}/setup

  function patchMavenSource() {
    grep "$source" $1 > /dev/null && \
      substituteInPlace $1 --replace "$2" "$3" 2>/dev/null
  }

  gradleFile="$1"

  # Some of those find something, some don't, that's fine.
  patchMavenSource "$gradleFile" 'mavenCentral()'       'mavenLocal()'
  patchMavenSource "$gradleFile" 'google()'             'mavenLocal()'
  patchMavenSource "$gradleFile" 'jcenter()'            'mavenLocal()'
  patchMavenSource "$gradleFile" 'gradlePluginPortal()' 'mavenLocal()'
'')

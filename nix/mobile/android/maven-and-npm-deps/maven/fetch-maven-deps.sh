#!/usr/bin/env bash

set -Eeu

# This script takes care of generating/updating the maven-inputs.txt file.
# For this, we:
#  1. query the projects in the main gradle project
#  2. loop through each of the projects, querying its dependencies
#  3. add each one to maven-inputs.txt

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
current_dir=$(cd "${BASH_SOURCE%/*}" && pwd)
gradle_opts="--console plain"
tmp_pom_filename=$(mktemp --tmpdir fetch-maven-deps-XXXX.pom)
deps_file_path=$(mktemp --tmpdir fetch-maven-deps-XXXX-deps.txt)

apacheUrl='https://repo.maven.apache.org/maven2'
fabricUrl='https://maven.fabric.io/public'
googleUrl='https://dl.google.com/dl/android/maven2'
gradlePluginsUrl='https://plugins.gradle.org/m2'
jcenterUrl='https://jcenter.bintray.com'
jitpackUrl='https://jitpack.io'

# Converts a URL to a Maven package ID (e.g. https://dl.google.com/dl/android/maven2/android/arch/core/common/1.0.0/common-1.0.0 -> android.arch.core:common:1.0.0)
function getPackageIdFromURL() {
  local url="$1"
  local path=$(echo $url | sed -E "s;($apacheUrl|$fabricUrl|$googleUrl|$gradlePluginsUrl|$jcenterUrl|$jitpackUrl)/(.+);\2;")

  IFS='/' read -ra tokens <<< "$path"
  local groupLength=$(( ${#tokens[@]} - 3 ))
  local groupId=''
  for ((i=0;i<$groupLength;i++)); do
    if [ $i -eq 0 ]; then
      groupId=${tokens[i]}
    else
      groupId="${groupId}.${tokens[i]}"
    fi
  done
  artifactId=${tokens[-3]}
  version="${tokens[-2]}"
  echo "$groupId:$artifactId:$version"
}

# Formats the components of a Maven package ID as a URL path component (e.g. android/arch/core/common/1.0.0/common-1.0.0)
function getPath() {
  local tokens=("$@")
  local groupId=${tokens[0]}
  local artifactId=${tokens[1]}
  local version=${tokens[2]}

  groupId=$(echo $groupId | tr '.' '/')
  echo "$groupId/$artifactId/$version/$artifactId-$version"
}

# Tries to download a POM to $tmp_pom_filename given a base URL (also checks for empty files)
function tryGetPOMFromURL() {
  local url="$1"

  rm -f $tmp_pom_filename
  curl --output $tmp_pom_filename --silent --fail "$url.pom" && test -s $tmp_pom_filename
}

# Given the components of a package ID, will loop through known repositories to figure out a source for the package
function determineArtifactUrl() {
  local tokens=("$@")
  local groupId=${tokens[0]}
  local artifactId=${tokens[1]}
  local version=${tokens[2]}

  local path=$(getPath "${tokens[@]}")
  if tryGetPOMFromURL "$googleUrl/$path"; then
    if [ "$path" = "com/google/firebase/firebase-analytics/16.0.3/firebase-analytics-16.0.3" ]; then
      # For some reason maven doesn't detect the correct version of firebase-analytics so we have to hardcode it
      echo "https://dl.google.com/dl/android/maven2/com/google/firebase/firebase-analytics/15.0.2/firebase-analytics-15.0.2"
    fi
    echo "$googleUrl/$path"
    return
  elif tryGetPOMFromURL "$jcenterUrl/$path"; then
    echo "$jcenterUrl/$path"
    return
  elif tryGetPOMFromURL "$apacheUrl/$path"; then
    echo "$apacheUrl/$path"
    return
  elif tryGetPOMFromURL "$fabricUrl/$path"; then
    echo "$fabricUrl/$path"
    return
  elif tryGetPOMFromURL "$jitpackUrl/$path"; then
    echo "$jitpackUrl/$path"
    return
  elif tryGetPOMFromURL "$gradlePluginsUrl/$path"; then
    echo "$gradlePluginsUrl/$path"
    return
  fi
  echo ""
}

# Executes a gradle dependencies command and returns the output package IDs
function runGradleDepsCommand() {
  echo "Computing maven dependencies with \`gradle $1 $gradle_opts\`..." > /dev/stderr
  # Add a comment header with the command we're running (useful for debugging)
  echo "# $1"

  # Run the gradle command and:
  # - remove lines that end with (*) or (n)
  # - keep only lines that start with \--- or +---
  # - remove lines that refer to a project
  # - extract the package name and version, ignoring version range indications, such as in `com.google.android.gms:play-services-ads:[15.0.1,16.0.0) -> 15.0.1`
  gradle $1 $gradle_opts \
    | grep --invert-match -E ".+ \([\*n]\)$" \
    | grep -e "[\\\+]---" \
    | grep --invert-match -e "--- project :" \
    | sed -E "s;.*[\\\+]--- ([^ ]+:)(.+ -> )?([^ ]+).*$;\1\3;"
}

mvn_tmp_repo=$(mktemp -d)
trap "rm -rf $mvn_tmp_repo $tmp_pom_filename $deps_file_path" ERR EXIT HUP INT

pushd $GIT_ROOT/android > /dev/null

projects=$(gradle projects $gradle_opts 2>&1 \
            | grep "Project ':" \
            | sed -E "s;^.--- Project '(\:[a-zA-Z0-9\-]+)';\1;")

echo -n > $deps_file_path
# TODO: try to limit unnecessary dependencies brought in by passing e.g. `--configuration releaseCompileClasspath` to the `gradle *:dependencies` command
runGradleDepsCommand 'buildEnvironment' >> $deps_file_path
for project in ${projects[@]}; do
  runGradleDepsCommand "${project}:buildEnvironment" >> $deps_file_path
  runGradleDepsCommand "${project}:dependencies" >> $deps_file_path
done

popd > /dev/null

# Read the deps file into memory, sorting and getting rid of comments, project names and duplicates
IFS=$'\n' deps=( $(cat $deps_file_path \
                   | grep --invert-match -E '^#.*$' \
                   | grep --invert-match -E '^[a-z]+$' \
                   | grep --invert-match -E '^:?[^:]+$' \
                   | sort -uV) )
unset IFS
rm -f $deps_file_path

lineCount=${#deps[@]}
currentLine=0
pstr="[=======================================================================]"

echo "Determining URLs for ${#deps[@]} packages..." > /dev/stderr
for dep in ${deps[@]}; do
  currentLine=$(( $currentLine + 1 ))
  pd=$(( $currentLine * 73 / $lineCount ))
  printf "\r%3d.%1d%% %.${pd}s" $(( $currentLine * 100 / $lineCount )) $(( ($currentLine * 1000 / $lineCount) % 10 )) $pstr > /dev/stderr

  [ -z "$dep" ] && continue
  if [[ "$dep" == *"["* ]] || [[ "$dep" == *"+"* ]] || [[ "$dep" == "#"* ]] || [[ "$dep" != *":"* ]] || [[ "$dep" == "status-im:"* ]]; then
    echo "Ignoring $dep" > /dev/stderr
    continue
  fi

  IFS=':' read -ra tokens <<< "$dep"
  groupId=${tokens[0]}
  [ -z "$groupId" ] && continue
  artifactId=${tokens[1]}
  version=$(echo "${tokens[2]}" | cut -d'@' -f1)

  artifactUrl=$(determineArtifactUrl $groupId $artifactId $version)
  if [ -z "$artifactUrl" ]; then
    echo "Failed to determine source of $dep, ignoring..." > /dev/stderr
  else
    echo "$artifactUrl"

    # It is not enough to output the dependencies in deps, we must also ask maven to report
    # the dependencies for each individual POM file. Instead of parsing the dependency tree itself though,
    # we look at what packages maven downloads from the internet into the local repo,
    # which avoids us having to do a deep search, and does not report duplicates
    additional_deps=( $(mvn dependency:tree -B -Dmaven.repo.local=$mvn_tmp_repo -f $tmp_pom_filename 2>&1 \
      | grep -E 'Downloaded from [^:]+: [^ ]+\.(pom|jar|aar)' \
      | sed -E "s;^\[INFO\] Downloaded from [^:]+: ([^ ]+)\.(pom|jar|aar) .*$;\1;") )

    for additional_dep_url in ${additional_deps[@]}; do
      additional_dep_id=$(getPackageIdFromURL $additional_dep_url)
      alreadyExists=0
      for _dep in ${deps[@]}; do
        if [ "$additional_dep_id" = "$_dep" ]; then
          alreadyExists=1
          break
        fi
      done
      [ $alreadyExists -eq 0 ] && echo "$additional_dep_url"
    done
  fi
done

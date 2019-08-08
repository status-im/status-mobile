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

function join_by { local IFS="$1"; shift; echo "$*"; }

mavenSources=(
  https://dl.google.com/dl/android/maven2 \
  https://jcenter.bintray.com \
  https://repo.maven.apache.org/maven2 \
  https://maven.fabric.io/public \
  https://jitpack.io \
  https://plugins.gradle.org/m2
)
mavenSourcesSedFilter=$(join_by '|' ${mavenSources[@]})

# Converts a URL to a Maven package ID (e.g. https://dl.google.com/dl/android/maven2/android/arch/core/common/1.0.0/common-1.0.0 -> android.arch.core:common:1.0.0)
function getPackageIdFromURL() {
  local url="$1"
  local path=$(echo $url | sed -E "s;($mavenSourcesSedFilter)/(.+);\2;")

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
  for mavenSourceUrl in ${mavenSources[@]}; do
    if tryGetPOMFromURL "$mavenSourceUrl/$path"; then
      if [ "$path" = "com/google/firebase/firebase-analytics/16.0.3/firebase-analytics-16.0.3" ]; then
        # For some reason maven doesn't detect the correct version of firebase-analytics so we have to hardcode it
        # TODO: See if we can get rid of this by upgrading to latest firebase
        echo "$mavenSourceUrl/com/google/firebase/firebase-analytics/15.0.2/firebase-analytics-15.0.2"
      fi
      echo "$mavenSourceUrl/$path"
      return
    fi
  done
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

function retrieveAdditionalDependencies() {
  # It is not enough to output the dependencies in deps, we must also ask maven to report
  # the dependencies for each individual POM file. Instead of parsing the dependency tree itself though,
  # we look at what packages maven downloads from the internet into the local repo,
  # which avoids us having to do a deep search, and does not report duplicates
  # tryGetPOMFromURL downloads the POM file into $tmp_pom_filename
  local additional_deps=( $(mvn dependency:tree -B -Dmaven.repo.local=$mvn_tmp_repo -f "$1" 2>&1 \
    | grep -E 'Downloaded from [^:]+: [^ ]+\.(pom|jar|aar)' \
    | sed -E "s;^\[INFO\] Downloaded from [^:]+: ([^ ]+)\.(pom|jar|aar) .*$;\1;") )

  for additional_dep_url in ${additional_deps[@]}; do
    local additional_dep_id=$(getPackageIdFromURL $additional_dep_url)

    # See if we already have this dependency in $deps
    local alreadyExists=0
    for _dep in ${deps[@]}; do
      if [ "$additional_dep_id" = "$_dep" ]; then
        alreadyExists=1
        break
      fi
    done
    [ $alreadyExists -eq 0 ] && echo "$additional_dep_url" || continue
  done
}

mvn_tmp_repo=$(mktemp -d)
trap "rm -rf $mvn_tmp_repo $tmp_pom_filename $deps_file_path" ERR EXIT HUP INT

rnModules=$(node ./node_modules/react-native/cli.js config | jq -r '.dependencies | keys | .[]')

pushd $GIT_ROOT/android > /dev/null

gradleProjects=$(gradle projects $gradle_opts 2>&1 \
                | grep "Project ':" \
                | sed -E "s;^.--- Project '\:([@_a-zA-Z0-9\-]+)';\1;")
projects=(${gradleProjects[@]} ${rnModules[@]})
IFS=$'\n' sortedProjects=($(sort -u <<<"${projects[*]}"))
unset IFS

echo -n > $deps_file_path
# TODO: try to limit unnecessary dependencies brought in by passing e.g. `--configuration releaseCompileClasspath` to the `gradle *:dependencies` command
runGradleDepsCommand 'buildEnvironment' >> $deps_file_path
for project in ${sortedProjects[@]}; do
  runGradleDepsCommand ${project}:buildEnvironment >> $deps_file_path
  runGradleDepsCommand ${project}:dependencies >> $deps_file_path
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
  # Ignore own dependencies (e.g. status-im:status-go:v0.30.0-beta.0)
  if [[ "$dep" == "status-im:"* ]]; then
    echo "Ignoring $dep" > /dev/stderr
    continue
  fi

  # Parse dependency ID into components (group ID, artifact ID, version)
  IFS=':' read -ra tokens <<< "$dep"
  groupId=${tokens[0]}
  [ -z "$groupId" ] && continue
  artifactId=${tokens[1]}
  version=$(echo "${tokens[2]}" | cut -d'@' -f1)

  artifactUrl=$(determineArtifactUrl $groupId $artifactId $version)
  if [ -z "$artifactUrl" ]; then
    # Some dependencies don't contain a normal format, so we ignore them (e.g. `com.squareup.okhttp:okhttp:{strictly`)
    echo "Failed to determine source of $dep, ignoring..." > /dev/stderr
    continue
  fi

  echo "$artifactUrl"

  retrieveAdditionalDependencies $tmp_pom_filename
done

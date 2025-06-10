#!/usr/bin/env bash
set -euo pipefail

# Fallback versions for each dependency
readonly FOOJAY_FALLBACK="0.5.0"
readonly KOTLIN_FALLBACK="1.8.0"
readonly TOOLS_FALLBACK="3.5.4"
readonly HERMES_FALLBACK="0.73.5"
readonly GRADLE_LINT_FALLBACK="31.1.1"

GIT_ROOT=$(cd "${BASH_SOURCE%/*}" && git rev-parse --show-toplevel)
cd "${GIT_ROOT}"

get_version() {
    local file="$1"
    local pattern="$2"
    local fallback="$3"
    local version

    if [[ ! -f "$file" ]]; then
        echo "$fallback"
        return 0
    fi

    version=$(grep "$pattern" "$file" 2>/dev/null | grep -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.*[0-9]*" | tail -n1 || echo "$fallback")
    echo "$version"
}

# https://github.com/googlesamples/android-custom-lint-rules/blob/be672cd747ecf13844918e1afccadb935f856a72/docs/api-guide/example.md.html#L112-L129
get_gradle_lint_version() {
  gradlePluginVersion=$(get_version "./nix/pkgs/aapt2/default.nix" "version =" $GRADLE_LINT_FALLBACK)
  major=$(echo "$gradlePluginVersion" | grep -o "^[0-9]*")
  #  lintVersion = gradlePluginVersion + 23.0.0
  major=$((major + 23))
  minor=$(echo "$gradlePluginVersion" | grep -o "\.[0-9]*\." | grep -o "[0-9]*")
  patch=$(echo "$gradlePluginVersion" | grep -o "[0-9]*$")

  echo "$major.$minor.$patch"
}

foojay_version=$(get_version "./node_modules/@react-native/gradle-plugin/settings.gradle.kts" "foojay.*version" "$FOOJAY_FALLBACK")
kotlin_version=$(get_version "./node_modules/@react-native/gradle-plugin/gradle/libs.versions.toml" "kotlin =" "$KOTLIN_FALLBACK")
tools_version=$(get_version "./patches/BlurView-build.gradle.patch" "gradle:" "$TOOLS_FALLBACK")
hermes_version=$(get_version "./node_modules/react-native/ReactAndroid/gradle.properties" "VERSION_NAME" "$HERMES_FALLBACK")
lint_version=$(get_gradle_lint_version)

cat << EOF
org.gradle.toolchains.foojay-resolver-convention:org.gradle.toolchains.foojay-resolver-convention.gradle.plugin:$foojay_version
org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlin_version
com.android.tools.build:gradle:$tools_version
com.facebook.react:hermes-android:$hermes_version
com.android.tools.lint:lint-gradle:$lint_version
EOF

# Maven dependencies Nix wrapper

## Overview

This folder contains the Nix expression (`maven-sources.nix`) that downloads all Maven dependencies required for Gradle, as well as the scripts used to generate that file, namely:

- `generate-nix.sh`: This is the main entry point script, which will use Gradle to determine the dependencies (into `maven-inputs.txt`), and `nix/tools/maven/maven-inputs2nix.sh` to generate `default.nix` from those dependencies.
- `fetch-maven-deps.sh`: This script does the heavy work of determining Gradle dependencies and outputting a `maven-inputs.txt` file listing the external URLs.
- `reactnative-android-native-deps.nix`: Contains the Nix attribute set used to download the React Native dependencies used in React Native Gradle scripts.
- `maven-inputs.txt`: A list of Maven dependenciy URLs that can be used by `nix/tools/maven/maven-inputs2nix.sh` to generate `maven-sources.nix`.

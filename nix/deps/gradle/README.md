# Description

This directory contains the tools and the data that allows Nix to manage Gradle project dependencies for the app.

# Usage

Simply calling `generate.sh` should result in a `deps.json` file which is used in the derivation that provides Gradle dependencies when building the Android app.

You can see in [`nix/mobile/android/release.nix`](../../mobile/android/release.nix) that it's used via the `-Dmaven.repo.local='${deps.gradle}'` Gradle flag.

# Files

First we have the files that store data on Gradle dependencies:

* `proj.list` - List of projects of which our Gradle project consists.
* `deps.list` - List of dependencies for our project and sub-projects.
* `deps.urls` - List of URLs to POMs for all of the dependencies.
* `deps.json` - Final file containing all of the necessary data for Nix.

Generating scripts:

- `generate.sh` - Main script which generates all the above files.
- `get_projects.sh` - Calls Gradle to get a list of sub-projects.
- `get_deps.sh` - Calls Gradle to get all the dependencies of sub-projects.
- `gradle_parser.awk` - An AWK script that parses above Gradle output.
- `url2json.sh` - Converts the list of URLs into a format consumable by Nix.
- `add_package.sh` - Allows for adding a missing package manually.

Finally we have the Nix derivation in `default.nix` which produces a derivation with all of the Gradle project dependencies:
```
 $ nix-build --no-out-link --attr pkgs.deps.gradle default.nix       
/nix/store/57g95ik19k6gs5w68yid3hzhsax60i3m-status-mobile-maven-deps

 $ ls -l /nix/store/57g95ik19k6gs5w68yid3hzhsax60i3m-status-mobile-maven-deps | head -n 5 
total 32
dr-xr-xr-x  3 root root  3 Jan  1  1970 android
dr-xr-xr-x 35 root root 35 Jan  1  1970 androidx
dr-xr-xr-x 15 root root 15 Jan  1  1970 com
dr-xr-xr-x  3 root root  3 Jan  1  1970 commons-cli
...
```

# Dependencies

One dependency these scripts require is the [go-maven-resolver](https://github.com/status-im/go-maven-resolver) which turns a list of packages into list of all URLs of POMs for them and their dependencies.

# Known Issues

One edge case that is currently not automatically handled by this setup is an addition of a new dependency that requires a version of Gradle we don't have. In that case running `make nix-update-gradle` would fail with:
```
AILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring project ':react-native-example-project'.
> Could not resolve all artifacts for configuration ':react-native-example-project:classpath'.
   > Could not find com.android.tools.build:gradle:3.4.0.
     Searched in the following locations:
       - file:/nix/store/34a4qd5qhg2a9kq6a0q9lp7hgmi48q4x-status-mobile-maven-deps/com/android/tools/build/gradle/3.4.0/gradle-3.4.0.pom
       - file:/nix/store/34a4qd5qhg2a9kq6a0q9lp7hgmi48q4x-status-mobile-maven-deps/com/android/tools/build/gradle/3.4.0/gradle-3.4.0.jar
     Required by:
         project :react-native-example-project
```
This happens because `get_projects.sh` and `get_deps.sh` depend on running Gradle to generate their output, and that will not work because Gradle cannot find the matching JAR for the new dependency.

You can use the `add_package.sh` script to add missing Gradle dependencies:
```
 > make shell TARGET=gradle
Configuring Nix shell for target 'gradle'...

[nix-shell:~/work/status-mobile]$ nix/deps/gradle/add_package.sh com.android.tools.build:gradle:3.5.3
Changes made:
 nix/deps/gradle/deps.urls | 24 ++++++++++++++++++++++++
 1 file changed, 24 insertions(+)

Regenerating Nix files...
Successfully added: com.android.tools.build:gradle:3.5.3

NOTICE: Running 'make nix-update-gradle' in a new shell is recommended.
```
Keep in mind that the changes made by this script do not affect the already spawned shell.

#!/usr/bin/env bash

# This defines URLs of Maven repos we know about and use.
# Should be ordered to reduce number of URL checks.
# Sorted on the frequency of different repos in deps.urls.
declare -a REPOS=(
  "https://repo.maven.apache.org/maven2"
  "https://dl.google.com/dl/android/maven2"
  "https://repository.sonatype.org/content/groups/sonatype-public-grid"
  "https://plugins.gradle.org/m2"
  "https://maven.java.net/content/repositories/releases"
  "https://jcenter.bintray.com"
  "https://jitpack.io"
  "https://repo1.maven.org/maven2"
)

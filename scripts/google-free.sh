#!/usr/bin/env bash

set -e

# Used by Clojure to condition code and Gradle to make dependencies optional
sed -i -e '$aGOOGLE_FREE=1' .env.release

# remove firebase (uses google dependencies) and google-services.json for the fdroid-build
yarn remove @react-native-firebase/app
yarn remove @react-native-firebase/messaging
rm android/app/google-services.json
rm android/app/googleServices.gradle

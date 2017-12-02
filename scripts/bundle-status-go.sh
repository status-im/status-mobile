#!/usr/bin/env sh

set -eou pipefail

# TODO Clean up with require STATUS_GO_HOME and STATUS_REACT_HOME

echo "[Assumes status-go is in sibling directory]"
echo "[Warning: iOS only for now]"

cd ..

# Build status-go artifact for iOS:
(cd status-go && make statusgo-ios-simulator)

# You should see iOS framework cross compilation done. This builds the following artifact:
#
# > (cd status-go && find . -iname "Statusgo.framework")
# ./build/bin/statusgo-ios-9.3-framework/Statusgo.framework
#
# Normally this is installed by Maven via Artifactory in this step
# mvn -f modules/react-native-status/ios/RCTStatus dependency:unpack
#
# Locally you can see it here:
# > (cd status-react && find . -iname "Statusgo.framework")
# ./modules/react-native-status/ios/RCTStatus/Statusgo.framework
# ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/Statusgo.framework
#
# Instead we are going to manually overwrite it.

# For Xcode to pick up the new version, remove the whole framework first:
rm -r status-react/modules/react-native-status/ios/RCTStatus/Statusgo.framework/ || true

# Then copy over framework:
cp -R status-go/build/bin/statusgo-ios-9.3-framework/Statusgo.framework status-react/modules/react-native-status/ios/RCTStatus/Statusgo.framework

# In Xcode, clean and build. If you have any scripts to do this, make sure that
# you don't accidentally run the mvn step to undo your manual install.
#

# It might also be a good idea to print something custom so you can easily tell
# the difference between an old and new version of status-go.

cd -

echo "[Done]"
echo "[You can now build in Xcode]"

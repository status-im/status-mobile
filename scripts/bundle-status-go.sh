#!/usr/bin/env sh

if [ -z $STATUS_GO_HOME ] ; then
    echo "Please define STATUS_GO_HOME"
    exit 1
fi
if [ -z $STATUS_REACT_HOME ] ; then
    echo "Please define STATUS_REACT_HOME"
    exit 1
fi
if [ $# -eq 0 ]; then
    echo "Please specify platforms to bundle as discrete arguments (ios, android)"
    exit 1
fi

set -euf

# Ensure we start with a clean state, so as to e.g., not reuse old native status-go bindings 
if [ -z $DONT_CLEAN ] ; then
  make clean
fi

for platform in "$@"; do
    case $platform in
    ios | android)
        echo "Bundling $platform platform"

        cd $STATUS_GO_HOME
        ;;
    *)
        echo "Undefined platform $platform"
        exit 1
    esac

    case $platform in
    ios)
        # Build status-go artifact for iOS:
        make statusgo-ios-simulator

        # You should see iOS framework cross compilation done. This builds the following artifact:
        #
        # > (cd status-go && find . -iname "Statusgo.framework")
        # ./build/bin/statusgo-ios-9.3-framework/Statusgo.framework
        #
        # You can get this by running:
        # make prepare-{ios,android}
        #
        # Locally you can see it here:
        # > (cd status-react && find . -iname "Statusgo.framework")
        # ./modules/react-native-status/ios/RCTStatus/Statusgo.framework
        # ./modules/react-native-status/ios/RCTStatus/Statusgo.framework/Statusgo.framework
        #
        # Instead we are going to manually overwrite it.

        # For Xcode to pick up the new version, remove the whole framework first:
        rm -r $STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus/Statusgo.framework/ || true

        # Then copy over framework:
        cp -R $STATUS_GO_HOME/build/bin/statusgo-ios-9.3-framework/Statusgo.framework $STATUS_REACT_HOME/modules/react-native-status/ios/RCTStatus/Statusgo.framework

        # In Xcode, clean and build. If you have any scripts to do this, make sure that
        # you don't accidentally run the mvn step to undo your manual install.
        #

        # It might also be a good idea to print something custom so you can easily tell
        # the difference between an old and new version of status-go.

        cd -

        echo "[Done]"
        echo "[You can now build in Xcode]"
        ;;
    android)
        # Build status-go artifact for Android:
        make statusgo-android

        target=$STATUS_REACT_HOME/modules/react-native-status/android/libs/status-im/status-go/local
        [ -d $target ] || mkdir -p $target
        # Copy over framework:
        cp -R $STATUS_GO_HOME/build/bin/statusgo.aar $target/status-go-local.aar

        # It might also be a good idea to print something custom so you can easily tell
        # the difference between an old and new version of status-go.

        cd -

        echo "[Done]"
        ;;
    esac
done

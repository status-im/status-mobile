#!/usr/bin/env sh

echo "Patching RCT Bundle URL provider to timeout if bundle update takes too long"
patch -p0 < rct-bundle-url-provider.patch
echo "Patching React Native Xcode script to not use xip.io"
patch -p0 < react-native-xcode.patch
echo "Ready to build and run for iOS device."

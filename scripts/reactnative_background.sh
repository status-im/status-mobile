#!/usr/bin/env bash
{ grep -q "<END>   Building Dependency Graph"; cat > /dev/null & } < <(react-native start);
echo "React Native Initialized"
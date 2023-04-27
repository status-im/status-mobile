{ mkShell, androidPkgs }:

mkShell {
  name = "android-sdk-shell";
  shellHook = ''

    export ANDROID_HOME="${androidPkgs.sdk}"
    export ANDROID_NDK_ROOT="${androidPkgs.ndk}"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"

    export PATH="$ANDROID_HOME/bin:$PATH"
    export PATH="$ANDROID_NDK_ROOT:$PATH"
    export PATH="$ANDROID_SDK_ROOT/tools:$PATH"
    export PATH="$ANDROID_SDK_ROOT/tools/bin:$PATH"
    export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
    export PATH="$ANDROID_SDK_ROOT/build-tools:$PATH"
  '';
}

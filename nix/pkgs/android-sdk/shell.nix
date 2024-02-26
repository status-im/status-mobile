{ mkShell, openjdk, androidPkgs }:

mkShell {
  name = "android-sdk-shell";
  buildInputs = [ openjdk ];

  shellHook = ''
    export ANDROID_HOME="${androidPkgs.sdk}"
    export ANDROID_NDK_ROOT="${androidPkgs.ndk}"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"

    export PATH="$ANDROID_NDK_ROOT:$PATH"
    export PATH="$ANDROID_SDK_ROOT/tools:$PATH"
    export PATH="$ANDROID_SDK_ROOT/tools/bin:$PATH"
    export PATH="$(echo $ANDROID_SDK_ROOT/cmdline-tools/*/bin):$PATH"
    export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
  '';
}

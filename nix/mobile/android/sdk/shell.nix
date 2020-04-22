{ mkShell, callPackage, openjdk }:

let
  licensedPkgs = callPackage ./licensed.nix { };
in
  mkShell {
    shellHook = ''
      export JAVA_HOME="${openjdk}"

      export ANDROID_HOME="${licensedPkgs}"
      export ANDROID_SDK_ROOT="${licensedPkgs}"
      export ANDROID_NDK_ROOT="${licensedPkgs}/ndk-bundle"

      export PATH="$ANDROID_HOME/bin:$PATH"
      export PATH="$ANDROID_NDK_ROOT:$PATH"
      export PATH="$ANDROID_SDK_ROOT/tools:$PATH"
      export PATH="$ANDROID_SDK_ROOT/tools/bin:$PATH"
      export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
      export PATH="$ANDROID_SDK_ROOT/build-tools:$PATH"
    '';
  }

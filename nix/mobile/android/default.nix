{ lib, pkgs, newScope, mkShell
, status-go, localMavenRepoBuilder
, gradle, androidPkgs, androidShell }:

let
  callPackage = newScope { inherit localMavenRepoBuilder; };

  # Import a jsbundle compiled out of clojure codebase
  jsbundle = callPackage ./jsbundle { };

  # Import a patched version of watchman (important for sandboxed builds on macOS)
  watchmanFactory = callPackage ./watchman.nix { };

  # Import a local patched version of node_modules, together with a local version of the Maven repo
  mavenAndNpmDeps = callPackage ./maven-and-npm-deps { };

  # TARGETS
  release = callPackage ./targets/release-android.nix {
    inherit gradle mavenAndNpmDeps jsbundle status-go watchmanFactory;
  };

  generate-maven-and-npm-deps-shell = callPackage ./maven-and-npm-deps/maven/shell.nix {
    inherit status-go;
  };

in {
  # TARGETS
  inherit release jsbundle generate-maven-and-npm-deps-shell;

  shell = mkShell {
    buildInputs = with pkgs; [
      mavenAndNpmDeps.drv openjdk gradle
      lsof  # used in start-react-native.sh
      flock # used in reset-node_modules.sh
    ];

    inputsFrom = [
      gradle
      release
      mavenAndNpmDeps.shell
      androidShell
    ];

    shellHook = ''
      export ANDROID_SDK_ROOT="${androidPkgs}"
      export ANDROID_NDK_ROOT="${androidPkgs}/ndk-bundle"

      # required by some makefile targets
      export STATUS_GO_ANDROID_LIBDIR=${status-go}

      # check if node modules changed and if so install them
      $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh \
        "${mavenAndNpmDeps.drv}/project"
    '';
  };
}

{ lib, pkgs, callPackage, mkShell
, status-go, gradle, androidPkgs, androidShell }:

let
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

in {
  # TARGETS
  inherit release jsbundle;

  shell = mkShell {
    buildInputs = with pkgs; [
      openjdk
      gradle
      lsof  # used in start-react-native.sh
      flock # used in reset-node_modules.sh
      mavenAndNpmDeps
    ];

    inputsFrom = [
      gradle
      release
      androidShell
    ];

    shellHook = ''
      export ANDROID_SDK_ROOT="${androidPkgs}"
      export ANDROID_NDK_ROOT="${androidPkgs}/ndk-bundle"

      export STATUSREACT_NIX_MAVEN_REPO="${mavenAndNpmDeps}/.m2/repository"

      # required by some makefile targets
      export STATUS_GO_ANDROID_LIBDIR=${status-go}

      {
        cd "$STATUS_REACT_HOME" 

        # Set up symlinks to mobile enviroment in project root 
        ln -sf ./mobile/js_files/* ./

        # check if node modules changed and if so install them
        $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh ${mavenAndNpmDeps}/project
      }
    '';
  };
}

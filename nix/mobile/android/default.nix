{ lib, pkgs, deps, callPackage, mkShell
, status-go, androidPkgs, androidShell, patchNodeModules }:

let
  # Import a jsbundle compiled out of clojure codebase
  jsbundle = callPackage ./jsbundle { };

  # Import a patched version of watchman (important for sandboxed builds on macOS)
  watchmanFactory = callPackage ./watchman.nix { };

  # Some node_modules have build.gradle files that reference remote repos.
  # This patches them to reference local repos only
  nodeJsModules = patchNodeModules deps.nodejs deps.gradle;

  # TARGETS
  release = callPackage ./release.nix {
    inherit jsbundle status-go watchmanFactory nodeJsModules;
  };

in {
  # TARGETS
  inherit release jsbundle nodeJsModules;

  shell = mkShell {
    buildInputs = with pkgs; [
      openjdk
      gradle
      lsof  # used in start-react-native.sh
      flock # used in nix/scripts/node_modules.sh
    ];

    inputsFrom = [
      release
      androidShell
    ];

    shellHook = ''
      export ANDROID_SDK_ROOT="${androidPkgs}"
      export ANDROID_NDK_ROOT="${androidPkgs}/ndk-bundle"

      export STATUSREACT_NIX_MAVEN_REPO="${deps.gradle}"

      # required by some makefile targets
      export STATUS_GO_ANDROID_LIBDIR=${status-go}

      {
        cd "$STATUS_REACT_HOME" 

        # Set up symlinks to mobile enviroment in project root 
        ln -sf ./mobile/js_files/* ./

        # check if node modules changed and if so install them
        $STATUS_REACT_HOME/nix/scripts/node_modules.sh ${nodeJsModules}
      }
    '';
  };
}

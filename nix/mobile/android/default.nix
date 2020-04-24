{ config, lib, callPackage, mkShell, mergeSh, flock, lsof, openjdk, gradle_5,
  status-go, localMavenRepoBuilder, projectNodePackage, androidPkgs, androidShell }:

let
  gradle = gradle_5; # Currently 5.6.4
  leinProjectDeps = import ../../lein/lein-project-deps.nix { };

  # Import a jsbundle compiled out of clojure codebase
  jsbundle = callPackage ./jsbundle/default.nix {
    inherit leinProjectDeps localMavenRepoBuilder projectNodePackage;
  };

  # Import a patched version of watchman (important for sandboxed builds on macOS)
  watchmanFactory = callPackage ./watchman.nix { };

  # Import a local patched version of node_modules, together with a local version of the Maven repo
  mavenAndNpmDeps = callPackage ./maven-and-npm-deps {
    inherit gradle localMavenRepoBuilder projectNodePackage;
  };

  # TARGETS
  release = callPackage ./targets/release-android.nix {
    inherit config gradle mavenAndNpmDeps jsbundle status-go watchmanFactory;
  };

  generate-maven-and-npm-deps-shell = callPackage ./maven-and-npm-deps/maven/shell.nix {
    inherit gradle projectNodePackage status-go;
  };

  buildInputs = [
    mavenAndNpmDeps.drv openjdk gradle
    lsof  # used in start-react-native.sh
    flock # used in reset-node_modules.sh
  ];

in {
  # TARGETS
  inherit release jsbundle generate-maven-and-npm-deps-shell buildInputs;

  shell = mergeSh
    (mkShell {
      inherit buildInputs;
      inputsFrom = [ release gradle ];
      shellHook = ''
        export ANDROID_SDK_ROOT="${androidPkgs}"
        export ANDROID_NDK_ROOT="${androidPkgs}/ndk-bundle"

        # check if node modules changed and if so install them
        $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh \
          "${mavenAndNpmDeps.drv}/project"
      '';
    })
    [ status-go.shell mavenAndNpmDeps.shell androidShell ];

  env = {
    shell = androidShell;
  };
}

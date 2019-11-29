{ config, lib, callPackage, mkShell, mergeSh, androidenv, flock, openjdk,
  status-go, localMavenRepoBuilder, projectNodePackage }:

let
  gradle = callPackage ./gradle.nix { };
  androidEnv = callPackage ./android-env.nix { };
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
    androidEnvShellHook = androidEnv.shell.shellHook;
  };

  generate-maven-and-npm-deps-shell = callPackage ./maven-and-npm-deps/maven/shell.nix {
    inherit gradle projectNodePackage status-go;
    androidEnvShellHook = androidEnv.shell.shellHook;
  };

  buildInputs = [
    mavenAndNpmDeps.drv openjdk gradle
    flock # used in reset-node_modules.sh
  ];

in {
  # TARGETS
  inherit release jsbundle generate-maven-and-npm-deps-shell buildInputs;
  inherit (androidEnv) androidComposition;

  shell = mergeSh
    (mkShell {
      inherit buildInputs;
      inputsFrom = [ release gradle ];
      shellHook = ''
        pushd "$STATUS_REACT_HOME" > /dev/null
        {
          ./scripts/generate-keystore.sh
          # check if node modules changed and if so install them
          ./nix/mobile/reset-node_modules.sh "${mavenAndNpmDeps.drv}/project"
        }
      '';
    })
    (lib.catAttrs "shell" [ status-go mavenAndNpmDeps androidEnv ]);

  adb = {
    shell = mkShell {
      buildInputs = [ androidEnv.drv ];
      inherit (androidEnv.shell) shellHook;
    };
  };
}

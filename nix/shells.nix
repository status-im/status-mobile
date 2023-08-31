# This file defines custom shells as well as shortcuts
# for accessing more nested shells.
{ config ? {}
, pkgs ? import ./pkgs.nix { inherit config; } }:

let
  inherit (pkgs) lib mkShell callPackage;

  # everything else we define in nix/ dir
  targets = callPackage ./targets.nix { inherit config; };

  # the default shell with most commonly used tools
  default = callPackage ./shell.nix { };

  # An attrset for easier merging with default shell
  shells = {
    inherit default;

    # for calling clojure targets in CI or Makefile
    clojure = mkShell {
      buildInputs = with pkgs; [ clojure flock maven openjdk ];
      # CLASSPATH from clojure deps with 'src' appended to find local sources.
      shellHook = with pkgs; ''
        export CLASS_PATH="$(find ${deps.clojure} -iname '*.jar' | tr '\n' ':')src"

        # Check if node modules changed and if so install them.
        "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${pkgs.deps.nodejs-patched}
      '';
    };

    # for 'make watchman-clean'
    watchman = mkShell {
      buildInputs = with pkgs; [ watchman ];
    };

    # for running fastlane commands alone
    fastlane = targets.mobile.fastlane.shell;

    # for running gradle by hand
    gradle = mkShell {
      buildInputs = with pkgs; [ gradle maven goMavenResolver htmlq ];
      shellHook = ''
        export STATUS_GO_ANDROID_LIBDIR="DUMMY"
        export STATUS_NIX_MAVEN_REPO="${pkgs.deps.gradle}"
        export ANDROID_SDK_ROOT="${pkgs.androidPkgs.sdk}"
        export ANDROID_NDK_ROOT="${pkgs.androidPkgs.ndk}"

        export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
        # WARNING: Unpatched Node.js deps allow Gradle to use remote repos.
        "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${pkgs.deps.nodejs}
        function restore_patched_modules() {
          "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${pkgs.deps.nodejs-patched}
        }
        trap restore_patched_modules EXIT
      '';
    };

    # for 'scripts/generate-keystore.sh'
    keytool = mkShell {
      buildInputs = with pkgs; [ openjdk apksigner ];
    };

    # for targets needing 'adb', 'apkanalyzer' and other SDK/NDK tools
    android-sdk = pkgs.androidShell;

    # helpers for use with target argument
    ios = targets.mobile.ios.shell;
    android = targets.mobile.android.shell;
    status-go = targets.status-go.mobile.android {};
  };

  # for merging the default shell with others
  mergeDefaultShell = (_: val: lib.mergeSh default [ val ]);

# values here can be selected using `nix-shell --attr shells.$TARGET default.nix`
# the nix/scripts/shell.sh wrapper does this for us and expects TARGET to be set
in lib.mapAttrs mergeDefaultShell shells

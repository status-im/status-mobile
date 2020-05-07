# This file defines custom shells as well as shortcuts
# for accessing more nested shells.
{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) lib stdenv mkShell;

  # everything else we define in nix/ dir
  targets = pkgs.callPackage ./targets.nix { inherit config; };

  # the default shell that is used when target is not specified
  # it is also merged with all the other shells
  default = mkShell {
    name = "status-react-shell"; # for identifying all shells

    buildInputs = with pkgs; lib.unique ([
      # core utilities that should always be present in a shell
      bash curl wget file unzip flock rsync
      git gnumake jq ncurses gnugrep parallel
      # build specific utilities
      clojure maven watchman
      # other nice to have stuff
      yarn nodejs python27
    ] # and some special cases
      ++ lib.optionals stdenv.isDarwin [ cocoapods clang ]
      ++ lib.optionals (!stdenv.isDarwin) [ gcc8 ]
    );

    # avoid terinal issues
    TERM="xterm";

    # default locale
    LANG="en_US.UTF-8";
    LANGUAGE="en_US.UTF-8";

    # just a nicety for easy access to node scripts
    shellHook = ''
      export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)
      export PATH="$STATUS_REACT_HOME/node_modules/.bin:$PATH"
    '';
  };

  # Combines with many other shells
  node-sh = mkShell {
    buildInputs = [ pkgs.androidPkgs ];
    shellHook = ''
      export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)
      $STATUS_REACT_HOME/nix/scripts/node_modules.sh ${pkgs.deps.nodejs}
    '';
  };

  # An attrset for easier merging with default shell
  shells = {
    nodejs = node-sh;

    # for calling clojure targets in CI or Makefile
    clojure = mkShell {
      buildInputs = with pkgs; [ clojure flock maven openjdk ];
      inputsFrom = [ node-sh ];
    };

    # for 'make watchman-clean'
    watchman = mkShell {
      buildInputs = with pkgs; [ watchman ];
    };

    # for running fastlane commands alone
    fastlane = targets.mobile.fastlane.shell;

    # for running gradle by hand
    gradle = mkShell {
      buildInputs = with pkgs; [ gradle maven ];
      inputsFrom = [ node-sh ];
      shellHook = ''
        export STATUS_GO_ANDROID_LIBDIR="DUMMY"
        export ANDROID_SDK_ROOT="${pkgs.androidPkgs}"
        export ANDROID_NDK_ROOT="${pkgs.androidPkgs}/ndk-bundle"
      '';
    };

    # for 'scripts/generate-keystore.sh'
    keytool = mkShell {
      buildInputs = with pkgs; [ openjdk8 ];
    };

    # for targets that need 'adb' and other SDK/NDK tools
    android-env = pkgs.androidShell;

    # helpers for use with target argument
    ios = targets.mobile.ios.shell;
    android = targets.mobile.android.shell;
    desktop = targets.desktop.shell;
    linux = targets.desktop.linux.shell;
    macos = targets.desktop.macos.shell;
    windows = targets.desktop.macos.shell;
  };

  # for merging the default shell with others
  mergeDefaultShell = (key: val: lib.mergeSh default [ val ]);

# values here can be selected using `nix-shell --attr shells.$TARGET default.nix`
# the nix/scripts/shell.sh wrapper does this for us and expects TARGET to be set
in {
  inherit default;
} // lib.mapAttrs mergeDefaultShell shells

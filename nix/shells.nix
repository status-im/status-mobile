# This file defines custom shells as well as shortcuts
# for accessing more nested shells.
{
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

let
  inherit (pkgs) lib stdenv;

  # everything else we define in nix/ dir
  targets = pkgs.callPackage ./targets.nix { inherit config; };

  # the default shell that is used when target is not specified
  # it is also merged with all the other shells
  default = pkgs.mkShell {
    name = "status-react-shell"; # for identifying all shells

    buildInputs = with pkgs; lib.unique ([
      # core utilities that should always be present in a shell
      bash curl wget file unzip flock git gnumake jq ncurses
      # build specific utilities
      clojure leiningen maven watchman
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

  # An attrset for easier merging with default shell
  shells = rec {
    inherit default;

    # for calling lein targets in CI or Makefile
    lein = pkgs.mkShell {
      buildInputs = with pkgs; [ clojure leiningen flock maven nodejs openjdk ];
    };

    # for 'make watchman-clean'
    watchman = pkgs.mkShell {
      buildInputs = [ pkgs.watchman ];
    };

    # for running fastlane commands alone
    fastlane = targets.mobile.fastlane.shell;

    # for 'scripts/generate-keystore.sh'
    keytool = pkgs.mkShell {
      buildInputs = [ pkgs.openjdk8 ];
    };

    # for targets that need 'adb'
    android-env = targets.mobile.android.env.shell;

    # helpers for use with target argument
    linux = targets.desktop.linux.shell;
    macos = targets.desktop.macos.shell;
    windows = targets.desktop.windows.shell;
    android = targets.mobile.android.shell;
    ios = targets.mobile.ios.shell;

    # all shells together depending on host OS
    all = lib.mergeSh (pkgs.mkShell {}) (lib.unique (
      lib.optionals stdenv.isLinux  [ android linux windows ] ++
      lib.optionals stdenv.isDarwin [ android macos ios ]
    ));
  };

  # for merging the default shell with others
  mergeDefaultShell = (name: value: lib.mergeSh default [ value ]);

# values here can be selected using `nix-shell --attr shells.$TARGET default.nix`
# the nix/scripts/shell.sh wrapper does this for us and expects TARGET to be set
in lib.mapAttrs mergeDefaultShell shells

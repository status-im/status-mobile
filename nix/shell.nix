#
# Defines the default shell that is used when target is not specified.
# It is also merged with all the other shells for a complete set of tools.
#
{ config ? {}
, pkgs ? import ./pkgs.nix { inherit config; } }:

let
  inherit (pkgs) mkShell;
in mkShell {
  name = "status-mobile-shell"; # for identifying all shells

  buildInputs = let
    appleSDKFrameworks = (with pkgs.darwin.apple_sdk.frameworks; [
      IOKit CoreServices
    ]);
  in
    with pkgs; lib.unique ([
      # core utilities that should always be present in a shell
      bash curl wget file unzip flock procps
      git gnumake jq ncurses gnugrep parallel
      lsof # used in start-react-native.sh
      # build specific utilities
      clojure maven watchman
      # other nice to have stuff
      yarn nodejs python310 maestro
      # Required for /scripts/compress_image.sh script
      imagemagick
    ] # and some special cases
      ++ lib.optionals   stdenv.isDarwin ([ cocoapods clang tcl idb-companion ] ++ appleSDKFrameworks)
      ++ lib.optionals (!stdenv.isDarwin) [ gcc8 ]
    );

  # avoid terminal issues
  TERM="xterm";

  # default locale
  LANG="en_US.UTF-8";
  LANGUAGE="en_US.UTF-8";

  # Important to load our own Nix binary cache.
  NIX_CONFIG = builtins.readFile ./nix.conf;

  # just a nicety for easy access to node scripts
  shellHook = ''
    export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
    export PATH="$STATUS_MOBILE_HOME/node_modules/.bin:$PATH"
  '';
}

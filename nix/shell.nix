#
# Defines the default shell that is used when target is not specified.
# It is also merged with all the other shells for a complete set of tools.
#
{ config ? {}
, pkgs ? import ./pkgs.nix { inherit config; } }:

let
  inherit (pkgs) mkShell;
  inherit (pkgs.stdenv) isDarwin;
in mkShell {
  name = "status-mobile-shell"; # for identifying all shells

  buildInputs = with pkgs; lib.unique ([
    # core utilities that should always be present in a shell
    bash curl wget file unzip flock procps
    git gnumake jq ncurses gnugrep parallel
    lsof # used in start-react-native.sh
    # build specific utilities
    clojure maven watchman
    # lint specific utilities
    clj-kondo zprint
    # other nice to have stuff
    yarn nodejs python310
  ] # and some special cases
    ++ lib.optionals stdenv.isDarwin [ cocoapods clang tcl ]
    ++ lib.optionals (!stdenv.isDarwin) [ gcc8 ]
  );

  # avoid terminal issues
  TERM="xterm";

  # default locale
  LANG="en_US.UTF-8";
  LANGUAGE="en_US.UTF-8";

  # just a nicety for easy access to node scripts
  shellHook = ''
    export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
    export PATH="$STATUS_MOBILE_HOME/node_modules/.bin:$PATH"
  '';

  # Sandbox causes Xcode issues on MacOS. Requires sandbox=relaxed.
  # Fixes: 'sandbox-exec: pattern serialization length 123479 exceeds maximum (65535)'
  # https://github.com/NixOS/nix/issues/4119
  __noChroot = isDarwin;
}

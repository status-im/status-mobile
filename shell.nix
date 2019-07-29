{ nixpkgs-bootstrap ? import ./nix/nixpkgs-bootstrap.nix { },
  pkgs ? nixpkgs-bootstrap.pkgs,
  target-os ? "none" }:

let
  project = import ./default.nix {
    inherit target-os pkgs nixpkgs-bootstrap;
    inherit (nixpkgs-bootstrap) config;
  };
  mkShell = pkgs.callPackage ./nix/bootstrapped-shell.nix {
    inherit stdenv target-os;
    inherit (pkgs) mkShell;
  };
  platform = pkgs.callPackage ./nix/platform.nix { inherit target-os; };
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  stdenv = pkgs.stdenvNoCC;

in mkShell {
  name = "status-react-shell";
  buildInputs = with pkgs; [
    # utilities
    bash
    curl
    file
    git
    gnumake
    jq
    ncurses
    lsof # used in scripts/start-react-native.sh
    ps # used in scripts/start-react-native.sh
    unzip
    wget

    clojure
    leiningen
    maven
    watchman
  ];
  inputsFrom = [ project.shell ];
  shellHook = project.shell.shellHook;
}

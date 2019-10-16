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
  # those should always be present in a shell
  coreInputs = with pkgs; [
    # utilities
    bash
    curl
    file
    flock
    git
    gnumake
    jq
    wget
    s3cmd
  ];

in mkShell {
  name = "status-react-shell";
  # none means we shouldn't include project specific deps
  buildInputs = if target-os == "none" then
    coreInputs
  else 
    with pkgs; [
      unzip
      ncurses
      lsof # used in scripts/start-react-native.sh
      ps # used in scripts/start-react-native.sh
      clojure
      leiningen
      maven
      watchman
    ] ++ coreInputs;

  inputsFrom = if target-os == "none" then
    []
  else 
    [ project.shell ];

  shellHook = project.shell.shellHook;
}

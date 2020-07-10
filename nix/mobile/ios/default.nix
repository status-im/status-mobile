{ callPackage, lib, mkShell, deps, pkgs
, status-go, status-go-shared, status-go-nim-status, fastlane }:

let
  inherit (lib) catAttrs unique;

  pod-shell = callPackage ./pod-shell.nix { };
  status-go-shell = callPackage ./status-go-shell.nix { 
    inherit status-go-shared status-go-nim-status; };

in {
  inherit pod-shell status-go-shell;

  shell = mkShell {
    buildInputs = with pkgs; [
      xcodeWrapper watchman bundler procps
      flock # used in nix/scripts/node_modules.sh
    ];

    inputsFrom = [
      fastlane.shell
      status-go-shell
      pod-shell
    ];

    shellHook = ''
      # check if node modules changed and if so install them
      ./nix/scripts/node_modules.sh "${deps.nodejs-patched}"
    '';
  };

}

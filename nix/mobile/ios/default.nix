{ callPackage, lib, mkShell, deps, pkgs
, status-go, fastlane }:

let
  inherit (lib) catAttrs unique;

  pod-shell = callPackage ./pod-shell.nix { };
  status-go-shell = callPackage ./status-go-shell.nix { inherit status-go; };

in {
  inherit pod-shell status-go-shell;

  shell = mkShell {
    buildInputs = with pkgs; [
      xcodeWrapper watchman bundler procps
      flock # used in nix/scripts/node_modules.sh
    ];

    # WARNING: Executes shellHook in reverse order.
    inputsFrom = [
      fastlane.shell
      pod-shell
      status-go-shell # Needs to run before pod-install
    ];

    shellHook = ''
      # check if node modules changed and if so install them
      ./nix/scripts/node_modules.sh "${deps.nodejs-patched}"
    '';
  };

}

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
      flock # used in reset-node_modules.sh
    ];

    inputsFrom = [
      fastlane.shell
      status-go-shell
      pod-shell
    ];

    shellHook = ''
      pushd "$STATUS_REACT_HOME" > /dev/null
      {
        # Set up symlinks to mobile enviroment in project root 
        ln -sf ./mobile/js_files/metro.config.js ./metro.config.js
        ln -sf ./mobile/js_files/package.json ./package.json
        ln -sf ./mobile/js_files/yarn.lock ./yarn.lock

        # check if node modules changed and if so install them
        ./nix/mobile/reset-node_modules.sh "${deps.nodejs}"
      }
      popd > /dev/null
    '';
  };

}

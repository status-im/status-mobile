{ pkgs, deps, callPackage, mkShell
, jsbundle, status-go, androidPkgs, androidShell }:

rec {
  release = callPackage ./release.nix {
    inherit jsbundle status-go;
  };

  shell = mkShell {
    buildInputs = with pkgs; [
      openjdk
      gradle
      lsof  # used in start-react-native.sh
      flock # used in nix/scripts/node_modules.sh
    ];

    inputsFrom = [
      (release {})
      androidShell
    ];

    shellHook = ''
      # check if node modules changed and if so install them
      $STATUS_MOBILE_HOME/nix/scripts/node_modules.sh ${deps.nodejs-patched}
    '';
  };
}

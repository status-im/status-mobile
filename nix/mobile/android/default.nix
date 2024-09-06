{ pkgs, deps, callPackage, mkShell
, jsbundle, status-go, androidPkgs, androidShell }:

rec {
  build = callPackage ./build.nix {
    inherit jsbundle status-go;
  };

  shell = mkShell {
    buildInputs = with pkgs; [
      openjdk_headless
      gradle
      lsof  # used in start-react-native.sh
      flock # used in nix/scripts/node_modules.sh
    ];

    inputsFrom = [
      (build {})
      androidShell
    ];

    shellHook = ''
      # check if node modules changed and if so install them
      $STATUS_MOBILE_HOME/nix/scripts/node_modules.sh ${deps.nodejs-patched}
    '';
  };
}

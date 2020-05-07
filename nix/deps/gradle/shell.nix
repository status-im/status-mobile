{ pkgs ? import ../../pkgs.nix { } }:

let
  status-go = pkgs.callPackage ../../status-go { };
  nodeJsDeps = pkgs.callPackage ../nodejs { };
in pkgs.mkShell {
  buildInputs = with pkgs; [
    curl flock # used in reset-node_modules.sh
    git gradle jq maven nodejs
  ];
  inputsFrom = [ pkgs.androidShell ];
  shellHook = ''
    # Gradle checks for this being set
    export STATUS_GO_ANDROID_LIBDIR=${status-go.mobile.android}

    # Necessary for reset-node_modules.sh
    export STATUS_REACT_HOME=$(realpath ../../../)

    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${nodeJsDeps}"
  '';
}

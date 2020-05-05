{ mkShell, pkgs
, projectNodePackage, androidShell, status-go }:

mkShell {
  buildInputs = with pkgs; [
    curl flock # used in reset-node_modules.sh
    git gradle jq maven nodejs
    projectNodePackage
  ];
  inputsFrom = [ androidShell ];
  shellHook = ''
    # Gradle check for this being set
    export STATUS_GO_ANDROID_LIBDIR=${status-go}

    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${projectNodePackage}"
  '';
}

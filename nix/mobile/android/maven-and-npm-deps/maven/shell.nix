{ lib, mkShell, pkgs, gradle, projectNodePackage, androidShell, status-go }:

lib.mergeSh (mkShell {
  buildInputs = with pkgs; [
    curl flock # used in reset-node_modules.sh
    git gradle jq maven nodejs
    projectNodePackage
  ];
  shellHook = ''
    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${projectNodePackage}"
  '';
}) [ status-go.shell androidShell ]

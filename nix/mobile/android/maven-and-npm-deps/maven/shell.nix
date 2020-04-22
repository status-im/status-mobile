{ mergeSh, mkShell, curl, flock, git, gradle, jq, maven, nodejs,
  projectNodePackage, androidShell, status-go }:

mergeSh (mkShell {
  buildInputs = [
    curl flock # used in reset-node_modules.sh
    git gradle jq maven nodejs
    projectNodePackage
  ];
  shellHook = ''
    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${projectNodePackage}"
  '';
}) [ status-go.shell androidShell ]

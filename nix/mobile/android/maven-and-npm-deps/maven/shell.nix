{ mkShell, curl, flock, git, gradle, jq, maven, nodejs,
  projectNodePackage, androidEnvShellHook, status-go }:

mkShell {
  buildInputs = [
    curl
    flock # used in reset-node_modules.sh
    git
    gradle
    jq
    maven
    nodejs
    projectNodePackage
  ];
  shellHook = ''
    ${androidEnvShellHook}
    ${status-go.shellHook}
    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${projectNodePackage}"
  '';
}

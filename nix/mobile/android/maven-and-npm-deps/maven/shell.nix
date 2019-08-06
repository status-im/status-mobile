{ mkShell, curl, git, gradle, jq, maven, nodejs,
  projectNodePackage, androidEnvShellHook, status-go }:

mkShell {
  buildInputs = [
    curl
    git
    gradle
    jq
    maven
    nodejs
    projectNodePackage
  ];
  shellHook =
    androidEnvShellHook +
    status-go.shellHook + ''
    $STATUS_REACT_HOME/nix/mobile/reset-node_modules.sh "${projectNodePackage}"
    chmod -R u+w ./node_modules/
  '';
}

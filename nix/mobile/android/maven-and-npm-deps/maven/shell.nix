{ mkShell, curl, git, gradle, maven,
  projectNodePackage, androidEnvShellHook, status-go }:

mkShell {
  buildInputs = [
    curl
    git
    gradle
    maven
    projectNodePackage
  ];
  shellHook =
    androidEnvShellHook +
    status-go.shellHook + ''
    rm -rf ./node_modules

    cp -a ${projectNodePackage}/node_modules/. ./node_modules/
    chmod -R u+w ./node_modules/
  '';
}

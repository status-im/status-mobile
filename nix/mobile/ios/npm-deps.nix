{ stdenvNoCC,
  bash, status-go, zlib,
  src, projectNodePackage }:

let
  deps = stdenvNoCC.mkDerivation {
    name = "patched-npm-modules";
    inherit src;
    buildInputs = [ bash zlib ] ++ status-go.buildInputs;
    buildPhases = [ "unpackPhase" "patchPhase" "installPhase" ];
    unpackPhase = ''
      runHook preUnpack

      cp -a $src/. .
      chmod u+w .

      # Copy fresh node_modules
      rm -rf ./node_modules
      mkdir -p ./node_modules
      cp -aL --copy-contents ${projectNodePackage}/libexec/${projectNodePackage.passthru.pname}/node_modules/. ./node_modules/
      chmod -R u+w ./node_modules
      cp -aL --copy-contents ${projectNodePackage}/libexec/${projectNodePackage.passthru.pname}/deps/${projectNodePackage.passthru.pname}/node_modules/. ./node_modules/

      # Adjust permissions
      chmod -R u+w .

      runHook postUnpack
    '';
    patchPhase = ''
      runHook prePatch

      patchShebangs .

      # Set up symlinks to mobile enviroment in project root 
      ln -sf ./mobile_files/package.json.orig package.json
      ln -sf ./mobile_files/metro.config.js
      ln -sf ./mobile_files/yarn.lock

      runHook postPatch
    '';
    installPhase = ''
      rm -rf $out
      mkdir -p $out
      cp -R node_modules/ $out
    '';

    # The ELF types are incompatible with the host platform, so let's not even try
    dontPatchELF = true;
    dontStripHost = true;

    # Take whole sources into consideration when calculating sha
    outputHashMode = "recursive";
    outputHashAlgo = "sha256";
  };

in {
  buildInputs = deps;

  shellHook = ''
    ln -sf $STATUS_REACT_HOME/mobile_files/package.json.orig $STATUS_REACT_HOME/package.json
    ln -sf $STATUS_REACT_HOME/mobile_files/metro.config.js $STATUS_REACT_HOME/metro.config.js
    ln -sf $STATUS_REACT_HOME/mobile_files/yarn.lock $STATUS_REACT_HOME/yarn.lock

    export PATH="$STATUS_REACT_HOME/node_modules/.bin:$PATH"
  '';
}

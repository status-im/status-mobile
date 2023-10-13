{ stdenv, lib, fetchurl, writeShellScriptBin }:

let
  inherit (lib)
    removeSuffix optionalString splitString concatMapStrings
    attrByPath attrValues last makeOverridable importJSON;

  # load dependencies
  deps = importJSON ./deps.json;

  script = writeShellScriptBin "create-local-maven-repo" (''
    mkdir -p $out
    cd $out
  '' +
  (concatMapStrings (dep: 
    let
      url = "${dep.host}/${dep.path}";
      jar = {
        sha1 = attrByPath [ "jar" "sha1" ] "" dep;
        sha256 = attrByPath [ "jar" "sha256" ] "" dep;
      };
      jarFile = optionalString (jar.sha256 != "") (
        fetchurl { url = "${url}.jar"; inherit (jar) sha256; }
      );
      fileName = last (splitString "/" dep.path);
      directory = removeSuffix fileName dep.path;
    in
      ''
        mkdir -p ${directory}

        ${optionalString (jarFile != "") ''
        ln -s "${jarFile}" "${dep.path}.jar"
        ''}
        ${optionalString (jar.sha1 != "") ''
        echo "${jar.sha1}" > "${dep.path}.jar.sha1"
        ''}
      '')
    deps));

in makeOverridable stdenv.mkDerivation {
  name = "status-mobile-clojure-deps";
  phases = [ "buildPhase" ];
  buildPhase = "${script}/bin/create-local-maven-repo";
}

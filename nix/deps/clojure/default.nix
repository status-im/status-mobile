{ stdenv, lib, fetchurl, writeShellScriptBin }:

let
  inherit (lib)
    removeSuffix optionalString splitString concatMapStrings
    attrByPath attrValues last makeOverridable importJSON;

  # load dependencies
  deps = importJSON ./deps.json;

  # some .jar files have an `-aot` suffix that doesn't work for .pom files
  getPOM = jarUrl: "${removeSuffix "-aot" jarUrl}.pom";

  script = writeShellScriptBin "create-local-maven-repo" (''
    mkdir -p $out
    cd $out
  '' +
  (concatMapStrings (dep: 
    let
      url = "${dep.host}/${dep.path}";
      pom = {
        sha1 = attrByPath [ "pom" "sha1" ] "" dep;
        sha256 = attrByPath [ "pom" "sha256" ] "" dep;
      };
      pom-download = optionalString (pom.sha256 != "") (
        fetchurl { url = getPOM url; inherit (pom) sha256; }
      );
      jar = {
        sha1 = attrByPath [ "jar" "sha1" ] "" dep;
        sha256 = attrByPath [ "jar" "sha256" ] "" dep;
      };
      jar-download = optionalString (jar.sha256 != "") (
        fetchurl { url = "${url}.jar"; inherit (jar) sha256; }
      );
      fileName = last (splitString "/" dep.path);
      directory = removeSuffix fileName dep.path;
    in
      ''
        mkdir -p ${directory}

        ${optionalString (pom-download != "") ''
        ln -s "${pom-download}" "${getPOM dep.path}"
        ''}
        ${optionalString (pom.sha1 != "") ''
        echo "${pom.sha1}" > "${getPOM dep.path}.sha1"
        ''}
        ${optionalString (jar-download != "") ''
        ln -s "${jar-download}" "${dep.path}.jar"
        ''}
        ${optionalString (jar.sha1 != "") ''
        echo "${jar.sha1}" > "${dep.path}.jar.sha1"
        ''}
      '')
    deps));

in makeOverridable stdenv.mkDerivation {
  name = "status-react-clojure-deps";
  phases = [ "buildPhase" ];
  buildPhase = "${script}/bin/create-local-maven-repo";
}

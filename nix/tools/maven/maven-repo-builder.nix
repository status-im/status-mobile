{ stdenv, lib, writeShellScriptBin, fetchurl }:

# Put the downloaded files in a fake Maven repository
name: source:

let
  script = writeShellScriptBin "create-local-maven-repo" (''
    mkdir -p $out
    cd $out
  '' +
  (lib.concatMapStrings (dep': 
    let
      dep = { postCopy = ""; } // dep';
      url = "${dep.host}/${dep.path}";
      pom = {
        sha1 = lib.attrByPath [ "pom" "sha1" ] "" dep;
        sha256 = lib.attrByPath [ "pom" "sha256" ] "" dep;
      };
      pom-download = lib.optionalString (pom.sha256 != "") (fetchurl { url = "${url}.pom"; inherit (pom) sha256; });
      jar = {
        sha1 = lib.attrByPath [ "jar" "sha1" ] "" dep;
        sha256 = lib.attrByPath [ "jar" "sha256" ] "" dep;
      };
      jar-download = lib.optionalString (jar.sha256 != "") (fetchurl { url = "${url}.${dep.type}"; inherit (jar) sha256; });
      fileName = lib.last (lib.splitString "/" dep.path);
      directory = lib.removeSuffix fileName dep.path;
    in
      ''
        mkdir -p ${directory}

        ${lib.optionalString (pom-download != "") ''
        cp -f "${pom-download}" "${dep.path}.pom"
        ''}
        ${lib.optionalString (pom.sha1 != "") ''
        echo "${pom.sha1}" > "${dep.path}.pom.sha1"
        ''}
        ${lib.optionalString (jar-download != "") ''
        cp -f "${jar-download}" "${dep.path}.${dep.type}"
        ''}
        ${lib.optionalString (jar.sha1 != "") ''
        echo "${jar.sha1}" > "${dep.path}.${dep.type}.sha1"
        ''}
        
        ${if dep.postCopy != "" then ''
          depPath="$PWD/${dep.path}"
          ${dep.postCopy}
          unset depPath
        '' else ""
        }
      '')
    (lib.attrValues source)));

in lib.makeOverridable stdenv.mkDerivation {
  inherit name;
  phases = [ "buildPhase" ];
  buildPhase = "${script}/bin/create-local-maven-repo";
}

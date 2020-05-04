{ stdenv, lib, writeShellScriptBin, fetchurl }:

# Put the downloaded files in a fake Maven repository
name: source:

let
  inherit (lib)
    removeSuffix optionalString splitString concatMapStrings
    attrByPath attrValues last makeOverridable;

  # some .jar files have an `-aot` suffix that doesn't work for .pom files
  getPOM = jarUrl: "${removeSuffix "-aot" jarUrl}.pom";

  script = writeShellScriptBin "create-local-maven-repo" (''
    mkdir -p $out
    cd $out
  '' +
  (concatMapStrings (dep': 
    let
      dep = { postCopy = ""; } // dep';
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
        fetchurl { url = "${url}.${dep.type}"; inherit (jar) sha256; }
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
        ln -s "${jar-download}" "${dep.path}.${dep.type}"
        ''}
        ${optionalString (jar.sha1 != "") ''
        echo "${jar.sha1}" > "${dep.path}.${dep.type}.sha1"
        ''}
        
        ${if dep.postCopy != "" then ''
          depPath="$PWD/${dep.path}"
          # postCopy can't modify the jar if it's a symlink
          rm "${dep.path}.${dep.type}"
          cp "${jar-download}" "${dep.path}.${dep.type}"
          ${dep.postCopy}
          unset depPath
        '' else ""
        }
      '')
    (attrValues source)));

in makeOverridable stdenv.mkDerivation {
  inherit name;
  phases = [ "buildPhase" ];
  buildPhase = "${script}/bin/create-local-maven-repo";
}

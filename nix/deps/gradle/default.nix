{ stdenv, lib, pkgs, fetchurl, writeShellScriptBin }:

let
  inherit (lib) concatStrings concatMapStrings mapAttrsToList makeOverridable;

  deps = lib.importJSON ./deps.json;

  script = writeShellScriptBin "create-local-maven-repo" (''
    mkdir -p $out
    cd $out
  '' +
    # For each dependency in deps.json.
    (concatMapStrings (dep: concatStrings
      # And for each file in the 'files' dict of given dependency.
      (mapAttrsToList (filename: hashes: let
        # Download given file(POM, JAR, nodeps JAR, or AAR).
        download = fetchurl {
          url = "${dep.repo}/${dep.path}/${filename}";
          inherit (hashes) sha256;
        };
        # And symlink it in the correct folder along with SHA1.
      in ''
        mkdir -p "${dep.path}"
        ln -s "${download}" "${dep.path}/${filename}"
        echo "${hashes.sha1}" > "${dep.path}/${filename}.sha1"
      '')
      dep.files)
    ) deps)
  );

in makeOverridable stdenv.mkDerivation {
  name = "status-mobile-maven-deps";

  buildInputs = [ pkgs.aapt2 ];

  phases = [ "buildPhase" "patchPhase" ];
  buildPhase = "${script}/bin/create-local-maven-repo";
  # Replace AAPT2 package only with our patched version from overlay.
  patchPhase = ''
    aapt2_dir=$out/com/android/tools/build/aapt2/${pkgs.aapt2.version}
    mkdir -p $aapt2_dir
    ln -sf ${pkgs.aapt2}/* $aapt2_dir
  '';
}

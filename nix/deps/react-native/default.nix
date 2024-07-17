{ stdenvNoCC, fetchurl }:

let
  inherit (builtins) map baseNameOf;

  deps = import ./deps.nix;
  fetchTarball = dep: with dep;
    fetchurl {
      inherit url sha256;
      name = "${name}-${baseNameOf url}";
    };

in stdenvNoCC.mkDerivation {
  name = "reactnative-android-deps";
  phases = [ "unpackPhase" ];
  srcs = map fetchTarball deps;
  unpackPhase = ''
    # Unpack all source archives.
    mkdir -p $out/deps
    cd $out/deps
    for tarball in $srcs; do
      unpackFile "$tarball" 2>/dev/null
    done
    cd ..

    export sourceRoot=$out/deps
    echo $sourceRoot
  '';
}

{stdenv, pkgs, writeScript}:
buildMap: name: stdenv.mkDerivation {
    name = "${name}-android";
    buildInputs = [ pkgs.coreutils ];
    builder = writeScript "${name}-android-builder.sh"
    ''
      source $stdenv/setup
      mkdir $out

      ln -s ${buildMap.x86} $out/x86
      ln -s ${buildMap.arm} $out/armeabi-v7a
      ln -s ${buildMap.arm64} $out/arm64-v8a
    '';
  }

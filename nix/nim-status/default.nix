{ mkShell
, callPackage
, writeScript
, pkgs
, stdenv}:
let
  buildNimStatus = platform: arch: callPackage ./nim-status.nix {
    inherit platform arch;
  };

  buildStatusGo = platform: arch: callPackage ./status-go.nix {
    inherit platform arch;
  };

  buildAndroid = callPackage ./buildAndroid.nix {};

  buildIos = callPackage ./buildIos.nix {};
in rec {
  nim-status = {
    android = {
      x86 = buildNimStatus "android" "386";
      arm = buildNimStatus "androideabi" "arm";
      arm64 = buildNimStatus "android" "arm64";
    };
    ios = {
      x86 = buildNimStatus "ios" "386";
      arm = buildNimStatus "ios" "arm";
      arm64 = buildNimStatus "ios" "arm64";
    };
  };

  status-go = {
    android = {
      x86 = buildStatusGo "android" "386";
      arm = buildStatusGo "androideabi" "arm";
      arm64 = buildStatusGo "android" "arm64";
    };
    ios = {
      x86 = buildStatusGo "ios" "386";
      arm = buildStatusGo "ios" "arm";
      arm64 = buildStatusGo "ios" "arm64";
    };
  };


  nim-status-android = buildAndroid nim-status.android "nim_status";
  nim-status-ios = buildIos nim-status.ios "nim_status";

  status-go-android = buildAndroid status-go.android "status";
  status-go-ios = buildIos status-go.ios "status";

  android = stdenv.mkDerivation {
      buildInputs = [ pkgs.coreutils ];
      name = "nim-status-go-android";
      builder = writeScript "nim-status-go-android-builder.sh"
      ''
        source $stdenv/setup

        mkdir $out
        for arch in "x86" "armeabi-v7a" "arm64-v8a"; do
          mkdir $out/$arch

          for filename in ${nim-status-android}/$arch/*; do
            ln -sf "$filename" $out/$arch/$(basename $filename)
          done

          for filename in ${status-go-android}/$arch/*; do
            ln -sf "$filename" $out/$arch/$(basename $filename)
          done
        done
      '';
  };

  ios = stdenv.mkDerivation {
      buildInputs = [ pkgs.coreutils ];
      name = "nim-status-go-ios";
      builder = writeScript "nim-status-go-ios-builder.sh"
      ''
        source $stdenv/setup
        mkdir $out
        for filename in ${nim-status-ios}/*; do
          ln -sf "$filename" $out/$(basename $filename)
        done

        for filename in ${status-go-ios}/*; do
          ln -sf "$filename" $out/$(basename $filename)
        done
      '';
  };
  shell = mkShell {
    inputsFrom = [ status-go-android status-go-ios ];
  };
}

{ lib, stdenv, newScope, xcodeWrapper, callPackage, mkShell, openjdk, androidPkgs }:

let
  inherit (lib)
    catAttrs concatStrings concatStringsSep fileContents makeBinPath
    getConfig optional attrValues mapAttrs attrByPath;

  # Metadata common to all builds of status-go
  meta = {
    description = "The Status Go module that consumes go-ethereum.";
    license = lib.licenses.mpl20;
    platforms = with lib.platforms; linux ++ darwin;
  };

  # Source can be changed with a local override from config
  source = callPackage ./source.nix { };

  
  callPackage = newScope { inherit meta source; };
in rec {

  android-x86 = callPackage ./build.nix { platform = "android"; arch = "386"; };
  android-arm = callPackage ./build.nix { platform = "androideabi"; arch = "arm"; };
  android-arm64 = callPackage ./build.nix { platform = "android"; arch = "arm64"; };

  android = stdenv.mkDerivation {
    name = "status-go.android";
    phases = [ "symlinkPhase" ];
    symlinkPhase = ''
      mkdir -p $out
      ln -s ${android-x86} $out/x86
      ln -s ${android-arm} $out/armeabi-v7a
      ln -s ${android-arm64} $out/arm64-v8a
    '';
  };

  ios-x86 = callPackage ./build.nix { platform = "ios"; arch = "386"; };
  ios-arm = callPackage ./build.nix { platform = "ios"; arch = "arm"; };
  ios-arm64 = callPackage ./build.nix { platform = "ios"; arch = "arm64"; };


  ios = stdenv.mkDerivation {
    name = "status-go.ios";
    phases = [ "symlinkPhase" ];
    inherit xcodeWrapper;
    symlinkPhase = ''
      mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 
      lipo -create ${ios-x86}/libstatus.a ${ios-arm}/libstatus.a ${ios-arm64}/libstatus.a -output $out/libstatus.a
      echo -e "#if TARGET_CPU_X86_64\n" >> $out/libstatus.h
      cat ${ios-x86}/libstatus.h >> $out/libstatus.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/libstatus.h
      cat ${ios-arm}/libstatus.h >> $out/libstatus.h
      echo -e "#else \n" >> $out/libstatus.h
      cat ${ios-arm64}/libstatus.h >> $out/libstatus.h
      echo -e "#endif\n" >> $out/libstatus.h
    '';
  };

  shell = mkShell {
    inputsFrom = [ android ios ];
  };
}

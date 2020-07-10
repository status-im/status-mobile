{ callPackage
, stdenv
, newScope
, lib
, xcodeWrapper
, meta, source }:
let
  inherit (lib) getAttr;
  callPackage = newScope { inherit meta source; };
  androidAbiMap = {
    "386" = "x86";
    "arm" = "armeabi-v7a";
    "arm64" = "arm64-v8a";
  };
in rec {
  android = {
    x86 = callPackage ./build.nix { platform = "android"; arch = "386"; };
    arm = callPackage ./build.nix { platform = "androideabi"; arch = "arm"; };
    arm64 = callPackage ./build.nix { platform = "android"; arch = "arm64"; };
  };

  ios = {
    x86 = callPackage ./build.nix { platform = "ios"; arch = "386"; };
    arm = callPackage ./build.nix { platform = "ios"; arch = "arm"; };
    arm64 = callPackage ./build.nix { platform = "ios"; arch = "arm64"; };
  };

  android-all = stdenv.mkDerivation {
    name = "status-go.shared.android-all";
    phases = [ "symlinkPhase" ];
    symlinkPhase = ''
      mkdir -p $out
      ln -s ${android.x86} $out/${getAttr "386" androidAbiMap}
      ln -s ${android.arm} $out/${getAttr "arm" androidAbiMap}
      ln -s ${android.arm64} $out/${getAttr "arm64" androidAbiMap}
    '';
  };

  ios-all = stdenv.mkDerivation {
    name = "status-go.shared.ios-all";
    phases = [ "symlinkPhase" ];
    inherit xcodeWrapper;
    symlinkPhase = ''
      mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 
      lipo -create ${ios.x86}/libstatus.a ${ios.arm}/libstatus.a ${ios.arm64}/libstatus.a -output $out/libstatus.a
      echo -e "#if TARGET_CPU_X86_64\n" >> $out/libstatus.h
      cat ${ios.x86}/libstatus.h >> $out/libstatus.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/libstatus.h
      cat ${ios.arm}/libstatus.h >> $out/libstatus.h
      echo -e "#else \n" >> $out/libstatus.h
      cat ${ios.arm64}/libstatus.h >> $out/libstatus.h
      echo -e "#endif\n" >> $out/libstatus.h
    '';
  };

}

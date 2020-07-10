{ newScope, meta, source, shared
, xcodeWrapper
, stdenv
, lib }:
let
  inherit (lib) getAttr;
  callPackage = newScope { inherit meta source shared; };
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
    name = "status-go.nim-status.android-all";
    phases = [ "symlinkPhase" ];
    symlinkPhase = ''
      mkdir -p $out
      ln -s ${android.x86} $out/${getAttr "386" androidAbiMap}
      ln -s ${android.arm} $out/${getAttr "arm" androidAbiMap}
      ln -s ${android.arm64} $out/${getAttr "arm64" androidAbiMap}
    '';
  };

  ios-all = stdenv.mkDerivation {
    name = "status-go.nim-status.ios-all";
    phases = [ "symlinkPhase" ];
    inherit xcodeWrapper;
    symlinkPhase = ''
      mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 
      lipo -create ${ios.x86}/libnim_status.a ${ios.arm}/libnim_status.a ${ios.arm64}/libnim_status.a -output $out/libnim_status.a
      echo -e "#if TARGET_CPU_X86_64\n" >> $out/nim_status.h
      cat ${ios.x86}/nim_status.h >> $out/nim_status.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/nim_status.h
      cat ${ios.arm}/nim_status.h >> $out/nim_status.h
      echo -e "#else \n" >> $out/nim_status.h
      cat ${ios.arm64}/nim_status.h >> $out/nim_status.h
      echo -e "#endif\n" >> $out/nim_status.h
      cp -r ${ios.arm64}/nimbase.h $out
    '';
  };
}


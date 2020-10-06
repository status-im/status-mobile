{ newScope
, xcodeWrapper
, stdenv
, lib }:
let
  inherit (lib) getAttr;
  callPackage = newScope {};
in rec {
  android-x86 = callPackage ./build.nix { platform = "android"; arch = "386"; };
  android-arm = callPackage ./build.nix { platform = "androideabi"; arch = "arm"; };
  android-arm64 = callPackage ./build.nix { platform = "android"; arch = "arm64"; };

  ios-x86 = callPackage ./build.nix { platform = "ios"; arch = "386"; };
  ios-arm = callPackage ./build.nix { platform = "ios"; arch = "arm"; };
  ios-arm64 = callPackage ./build.nix { platform = "ios"; arch = "arm64"; };

  android = stdenv.mkDerivation {
    name = "nim-status.android";
    phases = [ "symlinkPhase" ];
    symlinkPhase = ''
      mkdir -p $out
      ln -s ${android-x86} $out/x86
      ln -s ${android-arm} $out/armeabi-v7a
      ln -s ${android-arm64} $out/arm64-v8a
    '';
  };

  ios = stdenv.mkDerivation {
    name = "nim-status.ios";
    phases = [ "symlinkPhase" ];
    inherit xcodeWrapper;
    symlinkPhase = ''
      mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 
      # lipo merges arch-specific binaries into one fat iOS binary
      lipo -create ${ios-x86}/libnim_status.a \
           ${ios-arm}/libnim_status.a \
           ${ios-arm64}/libnim_status.a \
           -output $out/libnim_status.a
      echo -e "#if TARGET_CPU_X86_64\n" >> $out/nim_status.h
      cat ${ios-x86}/nim_status.h >> $out/nim_status.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/nim_status.h
      cat ${ios-arm}/nim_status.h >> $out/nim_status.h
      echo -e "#else \n" >> $out/nim_status.h
      cat ${ios-arm64}/nim_status.h >> $out/nim_status.h
      echo -e "#endif\n" >> $out/nim_status.h
      cp -r ${ios-arm64}/nimbase.h $out
    '';
  };
}


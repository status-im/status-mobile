{ lib, stdenv, writeScript, newScope, xcodeWrapper, mkShell }:

let
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

  # Symlink separate ABI builds to respective folders so that
  # Gradle native build can find them
  android = stdenv.mkDerivation {
    name = "status-go-android-builder";
    builder = writeScript "status-go-android-builder.sh"
    ''
      /bin/mkdir -p $out
      /bin/ln -s ${android-x86} $out/x86
      /bin/ln -s ${android-arm} $out/armeabi-v7a
      /bin/ln -s ${android-arm64} $out/arm64-v8a
    '';
  };

  ios-x86 = callPackage ./build.nix { platform = "ios"; arch = "386"; };
  ios-arm = callPackage ./build.nix { platform = "ios"; arch = "arm"; };
  ios-arm64 = callPackage ./build.nix { platform = "ios"; arch = "arm64"; };

  # Create a single multi-arch fat binary using lipo
  # Create a single header for multiple target archs
  # by utilizing C preprocessor conditionals
  ios = stdenv.mkDerivation {
    inherit xcodeWrapper;
    name = "status-go-ios-builder";
    builder = writeScript "status-go-ios-builder.sh"
    ''
      /bin/mkdir -p $out
      export PATH=${xcodeWrapper}/bin:$PATH 
      lipo -create ${ios-x86}/libstatus.a ${ios-arm}/libstatus.a ${ios-arm64}/libstatus.a -output $out/libstatus.a
      echo -e "#if TARGET_CPU_X86_64\n" >> $out/libstatus.h
      /bin/cat ${ios-x86}/libstatus.h >> $out/libstatus.h
      echo -e "#elif TARGET_CPU_ARM\n" >> $out/libstatus.h
      /bin/cat ${ios-arm}/libstatus.h >> $out/libstatus.h
      echo -e "#else \n" >> $out/libstatus.h
      /bin/cat ${ios-arm64}/libstatus.h >> $out/libstatus.h
      echo -e "#endif\n" >> $out/libstatus.h
    '';
  };

  shell = mkShell {
    inputsFrom = [ android ios ];
  };
}

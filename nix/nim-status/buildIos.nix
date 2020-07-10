{stdenv, pkgs, writeScript, writeText, xcodeWrapper}:
buildMap: name:
  # Create a single multi-arch fat binary using lipo
  # Create a single header for multiple target archs
  # by utilizing C preprocessor conditionals
  let
    headerIos = writeText "${name}.h" ''
      #if TARGET_CPU_X86_64
      ${builtins.readFile "${buildMap.x86}/${name}.h"}
      #elif TARGET_CPU_ARM
      ${builtins.readFile "${buildMap.arm}/${name}.h"}
      #else
      ${builtins.readFile "${buildMap.arm64}/${name}.h"}
      #endif
    '';
  in stdenv.mkDerivation {
    name = "${name}-ios";
    buildInputs = [ pkgs.coreutils xcodeWrapper ];
    builder = writeScript "${name}-ios-builder.sh"
    ''
      source $stdenv/setup
      mkdir $out

      # lipo merges arch-specific binaries into one fat iOS binary
      lipo -create ${buildMap.x86}/lib${name}.a \
           ${buildMap.arm}/lib${name}.a \
           ${buildMap.arm64}/lib${name}.a \
           -output $out/lib${name}.a

      cp ${headerIos} $out/${name}.h
      ${if name=="nim_status" then "cp ${buildMap.arm64}/nimbase.h $out" else ""}
    '';
}

{ stdenv, callPackage, meta, source, nimbusSource, nimCompiler, goBuildLdFlags }:

let 
  buildNimbusLc = { platform, targets }: builtins.map
      (target:
        let
          arch =
            if platform == "ios" then
              (if target == "iossimulator" && stdenv.hostPlatform.isx86
              then "x86" else "arm64")
            else builtins.elemAt (builtins.split "\/" target) 2 ;
        in
        callPackage ./buildNimbus.nix {
          inherit nimCompiler platform arch;
          srcRaw = nimbusSource;
        }
      )
      targets;
in {

  #ios-x86 = buildNimbusLc {platform = "ios"; targets = ["ios" "iossimulator"];};
  ios-x86 = buildNimbusLc {platform = "ios"; targets = ["iossimulator"];};
  ios-arm = buildNimbusLc {platform = "ios"; targets = ["ios"];};
  android-arm64 = buildNimbusLc {platform = "android"; targets = ["android/arm64"];};
  android-arm = buildNimbusLc {platform = "android"; targets = ["android/arm"];};


  android = callPackage ./build.nix {
    platform = "android";
    platformVersion = "23";
    #targets = [ "android/arm" "android/arm64" "android/386" ];
    targets = [ "android/arm64" ];
    outputFileName = "status-go-${source.shortRev}.aar";
    inherit meta source buildNimbusLc goBuildLdFlags;
  };

  ios = callPackage ./build.nix {
    platform = "ios";
    platformVersion = "8.0";
    targets = [ "iossimulator" ];
    outputFileName = "Statusgo.xcframework";
    inherit meta source buildNimbusLc goBuildLdFlags;
  };
}

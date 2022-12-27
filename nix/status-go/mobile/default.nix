{ callPackage, meta, source, goBuildLdFlags }:

{
  android = callPackage ./build.nix {
    platform = "android";
    platformVersion = "23";
    targets = [ "android/arm" "android/arm64" "android/386" ];
    outputFileName = "status-go-${source.shortRev}.aar";
    inherit meta source goBuildLdFlags;
  };

  ios = callPackage ./build.nix {
    platform = "ios";
    platformVersion = "8.0";
    targets = [ "ios" "iossimulator" ];
    outputFileName = "Statusgo.xcframework";
    inherit meta source goBuildLdFlags;
  };
}

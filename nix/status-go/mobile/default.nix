{ callPackage, meta, source, goBuildLdFlags }:

{
  android = {abis ? [ "armeabi-v7a" "arm64-v8a" "x86" ]}: callPackage ./build.nix {
    platform = "android";
    platformVersion = "23";
    # Hide different arch naming in gomobile from Android builds.
    targets = let
      abiMap = {
        "armeabi-v7a" = "android/arm";
        "arm64-v8a"   = "android/arm64";
        "x86"         = "android/386";
        "x86_64"      = "android/amd64";
        };
      in map (arch: abiMap."${arch}") abis;
    outputFileName = "status-go-${source.shortRev}.aar";
    inherit meta source goBuildLdFlags;
  };

  ios = {targets ? [ "ios/arm64" "iossimulator/amd64"]}: callPackage ./build.nix {
    platform = "ios";
    platformVersion = "8.0";
    outputFileName = "Statusgo.xcframework";
    inherit meta source goBuildLdFlags targets;
  };
}

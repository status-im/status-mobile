{ callPackage, meta, source, goBuildLdFlags }:

{
  android = callPackage ./build.nix {
    platform = "android";
    platformVersion = "23";
    architectures = [ "arm" "arm64" "386" ]; # 386 is for android simulator.
    outputFileName = "status-go-${source.shortRev}.aar";
    inherit meta source goBuildLdFlags;
  };

  ios = callPackage ./build.nix {
    platform = "ios";
    platformVersion = "8.0";
    architectures = [ "arm64" "amd64" ]; # amd64 is for ios simulator.
    outputFileName = "Statusgo.framework";
    inherit meta source goBuildLdFlags;
  };
}

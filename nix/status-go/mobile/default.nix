{ callPackage
, meta, source
, goBuildFlags
, goBuildLdFlags }:

{
  android = callPackage ./build.nix {
    platform = "android";
    # 386 is for android simulator
    architectures = [ "arm" "arm64" "386" ];
    outputFileName = "status-go-${source.shortRev}.aar";
    inherit meta source goBuildFlags goBuildLdFlags;
  };

  ios = callPackage ./build.nix {
    platform = "ios";
    # amd64 is for ios simulator
    architectures = [ "arm64" "amd64" ];
    outputFileName = "Statusgo.framework";
    inherit meta source goBuildFlags goBuildLdFlags;
  };
}

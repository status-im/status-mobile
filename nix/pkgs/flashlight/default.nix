{ lib
, stdenv
, fetchurl
, unzip
}:

let
  platform = lib.getAttr stdenv.hostPlatform.system {
    x86_64-linux = "linux";
    aarch64-linux = "linux";
    x86_64-darwin = "macos";
    aarch64-darwin = "macos";
  };

  filename = "flashlight-${platform}";

in stdenv.mkDerivation rec {
  pname = "flashlight";
  version = "0.18.0";
  # inspired by script at https://get.flashlight.dev/
  src = fetchurl {
    url = "https://github.com/bamlab/flashlight/releases/download/v${version}/${filename}.zip";
    hash = {
      linux = "sha256-W5+rpiVDRNeO5dAybCk4B/y8Q5GOtTwnLzweBUVX6mg=";
      macos = "sha256-bkGC96lO7wjHvLJSbcg9YYAI8mK9QFLxjEid9oDiNlY=";
    }.${platform};
  };

  nativeBuildInputs = [ unzip ];

  sourceRoot = ".";

  stripDebug = false;
  dontStrip = true;

  unpackPhase = ''
    runHook preUnpack
    mkdir -p source
    unzip $src -d source
    runHook postUnpack
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out/bin
    cp source/${filename} $out/bin/flashlight
    chmod +x $out/bin/flashlight

    runHook postInstall
  '';

  meta = with lib; {
    description = "Flashlight CLI tool";
    homepage = "https://github.com/bamlab/flashlight";
    license = with lib.licenses; [ mit ];
    maintainers = with lib.maintainers; [ siddarthkay ];
    platforms = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
  };
}

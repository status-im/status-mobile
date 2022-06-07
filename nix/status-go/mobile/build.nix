{ lib, utils, buildGoPackage, androidPkgs, openjdk, gomobile, xcodeWrapper
, meta
, source
, platform ? "android"
, platformVersion ? "23"
, architectures ? [ "arm64" "arm" "x86" ]
, goBuildFlags ? [ "-x" ]
, goBuildLdFlags ? [ ]
, outputFileName ? "status-go-${source.shortRev}-${platform}.aar" }:

# Path to the file containing secret environment variables
{ secretsFile ? "" }:

let
  inherit (lib) concatStringsSep optionalString optional;
  # formatted for use with -target
  targetArchs = map (a: "${platform}/${a}") architectures;

in buildGoPackage {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}-${platform}";

  inherit meta;
  inherit (source) src goPackagePath;

  extraSrcPaths = [ gomobile ];
  nativeBuildInputs = [ gomobile ]
    ++ optional (platform == "android") openjdk
    ++ optional (platform == "ios") xcodeWrapper;

  ldflags = concatStringsSep " " (goBuildLdFlags
    ++ lib.optionals (secretsFile != "") ["-X node.OpenseaKeyFromEnv=$OPENSEA_API_KEY"]);

  ANDROID_HOME = optionalString (platform == "android") androidPkgs.sdk;

  # Ensure XCode is present for iOS, instead of failing at the end of the build.
  preConfigure = optionalString (platform == "ios") utils.enforceXCodeAvailable;

  # If secretsFile is not set we use generate keystore.
  preBuild = if (secretsFile != "") then ''
    source "${secretsFile}"
  '' else ''
    echo "No secrets provided!"
  '';

  buildPhase = ''
    runHook preBuild
    echo -e "\nBuilding for targets: ${concatStringsSep "," targetArchs}\n"

    gomobile bind \
      ${concatStringsSep " " goBuildFlags} \
      -ldflags="$ldflags" \
      -target=${concatStringsSep "," targetArchs} \
      ${optionalString (platform == "android") "-androidapi=${platformVersion}"} \
      ${optionalString (platform == "ios") "-iosversion=${platformVersion}"} \
      -o ${outputFileName} \
      ${source.goPackagePath}/mobile

    runHook postBuild
  '';

  installPhase = ''
    mkdir -p $out
    cp -r ${outputFileName} $out/
  '';
}

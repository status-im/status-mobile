{ callPackage, lib, buildGoPackage
, androidPkgs, openjdk, gomobile, xcodeWrapper, removeReferencesTo
, meta
, source
, platform ? "android"
, platformVersion ? "23"
, targets ? [ "android/arm64" "android/arm" ]
, goBuildFlags ? [ ] # Use -v or -x for debugging.
, goBuildLdFlags ? [ ]
, outputFileName ? "status-go-${source.shortRev}-${platform}.aar" }:

let
  inherit (lib) concatStringsSep optionalString optional;
  isIOS = platform == "ios";
  isAndroid = platform == "android";
  enforceXCodeAvailable = callPackage ./enforceXCodeAvailable.nix { };

in buildGoPackage {
  pname = source.repo;
  version = "${source.cleanVersion}-${source.shortRev}-${platform}";

  inherit meta;
  inherit (source) src goPackagePath;

  # Sandbox causes Xcode issues on MacOS. Requires sandbox=relaxed.
  # https://github.com/status-im/status-mobile/pull/13912
  __noChroot = isIOS;

  extraSrcPaths = [ gomobile ];
  nativeBuildInputs = [ gomobile removeReferencesTo ]
    ++ optional isAndroid openjdk
    ++ optional isIOS xcodeWrapper;

  ldflags = concatStringsSep " " goBuildLdFlags;

  ANDROID_HOME = optionalString isAndroid androidPkgs.sdk;

  # Ensure XCode is present for iOS, instead of failing at the end of the build.
  preConfigure = optionalString isIOS enforceXCodeAvailable;

  buildPhase = ''
    runHook preBuild
    echo -e "\nBuilding $pname for: ${concatStringsSep "," targets}"

    gomobile bind \
      ${concatStringsSep " " goBuildFlags} \
      -ldflags="$ldflags" \
      -target=${concatStringsSep "," targets} \
      ${optionalString isAndroid "-androidapi=${platformVersion}" } \
      ${optionalString isIOS "-iosversion=${platformVersion}" } \
     -tags='${optionalString isIOS "nowatchdog"} gowaku_skip_migrations' \
      -o ${outputFileName} \
      ${source.goPackagePath}/mobile

    runHook postBuild
  '';

  installPhase = ''
    mkdir -p $out
    cp -r ${outputFileName} $out/
  '';

  # Drop govers from disallowedReferences.
  dontRenameImports = true;
  # Replace hardcoded paths to go package in /nix/store.
  preFixup = optionalString isIOS ''
    find $out -type f -exec \
      remove-references-to -t $disallowedReferences '{}' + || true
  '';
}

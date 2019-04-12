# target-os = [ 'windows' 'linux' 'macos' 'darwin' 'android' 'ios' ]
{ system ? builtins.currentSystem
, config ? {}, overlays ? []
, pkgs ? (import <nixpkgs> { inherit system config overlays; })
, target-os }:

with pkgs;
  let
    sanitized-target-os = 
      if (builtins.any (os: target-os == os) [ "linux" "android" "windows" "macos" "darwin" "ios" "all" ])
      then target-os
      else throw "Unknown value for target-os: '${target-os}'";
    # based on the value passed in through target-os, check if we're targetting a desktop platform
    targetDesktop = {
      "linux" = true;
      "windows" = true;
      "macos" = true;
      "darwin" = true;
      "all" = true;
    }.${sanitized-target-os} or false;
    # based on the value passed in through target-os, check if we're targetting a mobile platform
    targetMobile = {
      "android" = true;
      "ios" = true;
      "all" = true;
    }.${sanitized-target-os} or false;
    # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
    _stdenv = stdenvNoCC;
    statusDesktop = callPackage ./nix/desktop { inherit target-os; stdenv = _stdenv; };
    statusMobile = callPackage ./nix/mobile { inherit target-os status-go; androidPkgs = androidComposition; stdenv = _stdenv; };
    status-go = callPackage ./nix/status-go { inherit (xcodeenv) composeXcodeWrapper; inherit xcodewrapperArgs; androidPkgs = androidComposition; };
    nodejs' = pkgs.nodejs-10_x;
    yarn' = yarn.override { nodejs = nodejs'; };
    nodeInputs = import ./nix/global-node-packages/output {
      # The remaining dependencies come from Nixpkgs
      inherit pkgs;
      nodejs = nodejs';
    };
    nodePkgBuildInputs = [
      nodejs'
      python27 # for e.g. gyp
      yarn'
    ] ++ (map (x: nodeInputs."${x}") (builtins.attrNames nodeInputs));
    xcodewrapperArgs = {
      version = "10.1";
    };
    xcodeWrapper = xcodeenv.composeXcodeWrapper xcodewrapperArgs;
    androidComposition = androidenv.composeAndroidPackages {
      toolsVersion = "26.1.1";
      platformToolsVersion = "28.0.2";
      buildToolsVersions = [ "28.0.3" ];
      includeEmulator = false;
      platformVersions = [ "26" "27" ];
      includeSources = false;
      includeDocs = false;
      includeSystemImages = false;
      systemImageTypes = [ "default" ];
      abiVersions = [ "armeabi-v7a" ];
      lldbVersions = [ "2.0.2558144" ];
      cmakeVersions = [ "3.6.4111459" ];
      includeNDK = true;
      ndkVersion = "19.2.5345600";
      useGoogleAPIs = false;
      useGoogleTVAddOns = false;
      includeExtras = [ "extras;android;m2repository" "extras;google;m2repository" ];
    };

  in _stdenv.mkDerivation rec {
    name = "status-react-build-env";

    buildInputs = with _stdenv; [
      clojure
      leiningen
      maven
      watchman

      status-go
    ] ++ nodePkgBuildInputs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional (!isDarwin) gcc7
      ++ lib.optionals targetDesktop statusDesktop.buildInputs
      ++ lib.optionals targetMobile statusMobile.buildInputs;
    shellHook =
      status-go.shellHook +
      ''
        export STATUS_GO_INCLUDEDIR=${status-go}/include
        export STATUS_GO_LIBDIR=${status-go}/lib
        export STATUS_GO_BINDIR=${status-go.bin}/bin
      '' +
      lib.optionalString targetDesktop statusDesktop.shellHook +
      lib.optionalString targetMobile statusMobile.shellHook;
    hardeningDisable = status-go.hardeningDisable;
  }

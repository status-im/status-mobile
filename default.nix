# target-os = [ 'windows' 'linux' 'macos' 'android' 'ios' ]
{ pkgs ? import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "db492b61572251c2866f6b5e6e94e9d70e7d3021";
    sha256 = "188r7gbcrxi20nj6xh9bmdf3lbjwb94v9s0wpacl7q39g1fca66h";
  }) { config = { android_sdk.accept_license = true; }; },
  target-os ? "" }:

with pkgs;
  let
    targetDesktop = {
      "linux" = true;
      "windows" = true;
      "macos" = true;
      "" = true;
    }.${target-os} or false;
    targetMobile = {
      "android" = true;
      "ios" = true;
      "" = true;
    }.${target-os} or false;
    # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
    _stdenv = if target-os == "ios" || target-os == "" then stdenvNoCC else stdenv;
    statusDesktop = callPackage ./nix/desktop { inherit target-os; stdenv = _stdenv; };
    statusMobile = callPackage ./nix/mobile { inherit target-os status-go; androidPkgs = androidComposition; stdenv = _stdenv; };
    status-go = callPackage ./nix/status-go { inherit (xcodeenv) composeXcodeWrapper; inherit xcodewrapperArgs; androidPkgs = androidComposition; };
    nodeInputs = import ./nix/global-node-packages/output {
      # The remaining dependencies come from Nixpkgs
      inherit pkgs nodejs;
    };
    nodePkgs = [
      nodejs
      python27 # for e.g. gyp
      yarn
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
    name = "env";
    env = buildEnv { name = name; paths = buildInputs; };
    buildInputs = with _stdenv; [
      bash
      clojure
      curl
      git
      jq
      leiningen
      lsof # used in scripts/start-react-native.sh
      maven
      ncurses
      ps # used in scripts/start-react-native.sh
      watchman
      unzip
      wget

      status-go
    ] ++ nodePkgs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional targetDesktop statusDesktop.buildInputs
      ++ lib.optional targetMobile statusMobile.buildInputs;
    shellHook =
      ''
        set -e
      '' +
      status-go.shellHook +
      ''
        export STATUS_GO_INCLUDEDIR=${status-go}/include
        export STATUS_GO_LIBDIR=${status-go}/lib
        export STATUS_GO_BINDIR=${status-go.bin}/bin
      '' +
      lib.optionalString targetDesktop statusDesktop.shellHook +
      lib.optionalString targetMobile statusMobile.shellHook +
      ''
        if [ -n "$ANDROID_SDK_ROOT" ] && [ ! -d "$ANDROID_SDK_ROOT" ]; then
          ./scripts/setup # we assume that if the Android SDK dir does not exist, setup script needs to be run
        fi
        set +e
      '';
    hardeningDisable = status-go.hardeningDisable;
  }

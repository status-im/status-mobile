{ target-os, stdenv, callPackage,
  buildGoPackage, go, fetchFromGitHub, openjdk,
  androidPkgs, xcodeWrapper }:

let
  inherit (stdenv.lib) catAttrs concatStrings fileContents importJSON makeBinPath optional optionalString strings;
  platform = callPackage ../platform.nix { inherit target-os; };
  utils = callPackage ../utils.nix { inherit xcodeWrapper; };
  gomobile = callPackage ./gomobile { inherit (androidPkgs) platform-tools; inherit target-os xcodeWrapper utils buildGoPackage; };
  buildStatusGoDesktopLib = callPackage ./build-desktop-status-go.nix { inherit buildGoPackage go xcodeWrapper utils; };
  buildStatusGoMobileLib = callPackage ./build-mobile-status-go.nix { inherit buildGoPackage go gomobile xcodeWrapper utils; };
  extractStatusGoConfig = callPackage ./extract-status-go-config.nix { inherit (stdenv) lib; };
  versionJSON = importJSON ../../status-go-version.json; # TODO: Simplify this path search with lib.locateDominatingFile
  owner = versionJSON.owner;
  repo = versionJSON.repo;
  version = versionJSON.version;
  sha256 = versionJSON.src-sha256;
  rev = versionJSON.commit-sha1;
  shortRev = strings.substring 0 7 rev;
  goPackagePath = "github.com/${owner}/${repo}";
  src = fetchFromGitHub { inherit rev owner repo sha256; name = "${repo}-${shortRev}-source"; };
  # Replace src value with the path to a local status-go repository if you want to perform a build against it, e.g.
  #src = /home/<user>/go/src/github.com/status-im/status-go;

  mobileConfigs = {
    android = {
      name = "android";
      outputFileName = "status-go-${shortRev}.aar";
      envVars = [
        "ANDROID_HOME=${androidPkgs.androidsdk}/libexec/android-sdk"
        "ANDROID_NDK_HOME=${androidPkgs.ndk-bundle}/libexec/android-sdk/ndk-bundle"
        "PATH=${makeBinPath [ openjdk ]}:$PATH"
      ];
      gomobileExtraFlags = [ "-androidapi 18" ];
    };
    ios = {
      name = "ios";
      outputFileName = "Statusgo.framework";
      envVars = [];
      gomobileExtraFlags = [ "-iosversion=8.0" ];
    };
  };
  hostConfigs = {
    darwin = {
      name = "macos";
      allTargets = [ status-go-packages.desktop status-go-packages.ios status-go-packages.android ];
    };
    linux = {
      name = "linux";
      allTargets = [ status-go-packages.desktop status-go-packages.android ];
    };
  };
  currentHostConfig = if stdenv.isDarwin then hostConfigs.darwin else hostConfigs.linux;

  goBuildFlags = "-v";
  # TODO: Manage to pass "-s -w" to -ldflags. Seems to only accept a single flag
  goBuildLdFlags = "-ldflags=-s";

  statusGoArgs = { inherit owner repo rev version goPackagePath src goBuildFlags goBuildLdFlags; };
  status-go-packages = {
    desktop = buildStatusGoDesktopLib (statusGoArgs // {
      outputFileName = "libstatus.a";
      hostSystem = stdenv.hostPlatform.system;
      host = currentHostConfig.name;
    });

    android = buildStatusGoMobileLib (statusGoArgs // {
      host = mobileConfigs.android.name;
      config = mobileConfigs.android;
    });

    ios = buildStatusGoMobileLib (statusGoArgs // {
      host = mobileConfigs.ios.name;
      config = mobileConfigs.ios;
    });
  };

  buildInputs = if target-os == "android" then
    android.buildInputs
  else if target-os == "ios" then
    ios.buildInputs
  else if target-os == "all" then
    currentHostConfig.allTargets
  else if platform.targetDesktop then
    desktop.buildInputs
  else
    throw "Unexpected target platform ${target-os}";
  android = {
    buildInputs = optional platform.targetAndroid [ status-go-packages.android ];
    shellHook =
      optionalString platform.targetAndroid ''
        # These variables are used by the Status Android Gradle build script in android/build.gradle
        export STATUS_GO_ANDROID_LIBDIR=${status-go-packages.android}/lib
      '';
  };
  ios = {
    buildInputs = optional platform.targetIOS [ status-go-packages.ios ];
    shellHook =
      optionalString platform.targetIOS ''
        # These variables are used by the iOS build preparation section in nix/mobile/ios/default.nix
        export RCTSTATUS_FILEPATH=${status-go-packages.ios}/lib/Statusgo.framework
      '';
  };
  desktop = {
    buildInputs = optional platform.targetDesktop [ status-go-packages.desktop ];
    shellHook =
      optionalString platform.targetDesktop ''
        # These variables are used by the Status Desktop CMake build script in modules/react-native-status/desktop/CMakeLists.txt
        export STATUS_GO_DESKTOP_INCLUDEDIR=${status-go-packages.desktop}/include
        export STATUS_GO_DESKTOP_LIBDIR=${status-go-packages.desktop}/lib
      '';
  };
  platforms = [ android ios desktop ];

in {
  inherit buildInputs;
  shellHook = concatStrings (catAttrs "shellHook" platforms);

  # CHILD DERIVATIONS
  inherit android ios desktop;
}

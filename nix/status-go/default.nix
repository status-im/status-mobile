{ target-os, config, stdenv, callPackage,
  buildGoPackage, go, fetchFromGitHub, openjdk,
  androidPkgs, xcodeWrapper }:

let
  inherit (stdenv.lib)
    catAttrs concatStrings fileContents importJSON makeBinPath
    optional optionalString removePrefix strings attrValues mapAttrs attrByPath
    traceValFn;

  platform = callPackage ../platform.nix { inherit target-os; };
  utils = callPackage ../utils.nix { inherit xcodeWrapper; };
  gomobile = callPackage ./gomobile { inherit (androidPkgs) platform-tools; inherit target-os xcodeWrapper utils buildGoPackage; };
  buildStatusGoDesktopLib = callPackage ./build-desktop-status-go.nix { inherit buildGoPackage go xcodeWrapper utils; };
  buildStatusGoMobileLib = callPackage ./build-mobile-status-go.nix { inherit buildGoPackage go gomobile xcodeWrapper utils; };
  srcData =
    # If config.status_go.src_override is defined, instruct Nix to use that path to build status-go
    if (attrByPath ["status_go" "src_override"] "" config) != "" then rec {
        owner = "status-im";
        repo = "status-go";
        version = "develop";
        goPackagePath = "github.com/${owner}/${repo}";
        rev = "unknown";
        shortRev = "unknown";
        versionName = "develop";
        src = stdenv.lib.cleanSource "${traceValFn (path: "Using local ${repo} sources from ${path}\n") config.status_go.src_override}";
    } else
      # Otherwise grab it from the location defined by status-go-version.json
      let
        versionJSON = importJSON ../../status-go-version.json; # TODO: Simplify this path search with lib.locateDominatingFile
        versionRegex = "^v?[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+(-[[:alnum:].]+)$";
        sha256 = versionJSON.src-sha256;
      in rec {
        inherit (versionJSON) owner repo version;
        goPackagePath = "github.com/${owner}/${repo}";
        rev = versionJSON.commit-sha1;
        shortRev = strings.substring 0 7 rev;
        versionName = if (builtins.match versionRegex version) != null
                      then removePrefix "v" version # Geth forces a 'v' prefix
                      else "develop"; # to reduce metrics cardinality in Prometheus
        src = fetchFromGitHub { inherit rev owner repo sha256; name = "${repo}-${srcData.shortRev}-source"; };
      };

  mobileConfigs = {
    android = {
      name = "android";
      outputFileName = "status-go-${srcData.shortRev}.aar";
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
  # status-go params to be set at build time, important for About section and metrics
  goBuildParams = {
    GitCommit = srcData.rev;
    Version = srcData.versionName;
  };
  # These are necessary for status-go to show correct version
  paramsLdFlags = attrValues (mapAttrs (name: value:
    "-X github.com/status-im/status-go/params.${name}=${value}"
  ) goBuildParams);

  goBuildLdFlags = paramsLdFlags ++ [
    "-s" # -s disabled symbol table
    "-w" # -w disables DWARF debugging information
  ];

  statusGoArgs = { inherit (srcData) src owner repo rev version goPackagePath; inherit goBuildFlags goBuildLdFlags; };
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

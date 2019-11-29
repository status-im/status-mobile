{ config, stdenv, callPackage, mkShell, mergeSh, buildGoPackage, go,
  fetchFromGitHub, mkFilter, openjdk, androidPkgs, xcodeWrapper }:

let
  inherit (stdenv.lib)
    catAttrs concatStrings fileContents importJSON makeBinPath
    optional optionalString strings attrValues mapAttrs attrByPath
    traceValFn;

  utils = callPackage ./utils.nix { inherit xcodeWrapper; };
  gomobile = callPackage ./gomobile { inherit (androidPkgs) platform-tools; inherit xcodeWrapper utils buildGoPackage; };
  buildStatusGoDesktopLib = callPackage ./build-desktop-status-go.nix { inherit buildGoPackage go xcodeWrapper utils; };
  buildStatusGoMobileLib = callPackage ./build-mobile-status-go.nix { inherit buildGoPackage go gomobile xcodeWrapper utils; };
  srcData =
    # If config.status_go.src_override is defined, instruct Nix to use that path to build status-go
    if (attrByPath ["status_go" "src_override"] "" config) != "" then rec {
        owner = "status-im";
        repo = "status-go";
        rev = "unknown";
        shortRev = "unknown";
        rawVersion = "develop";
        cleanVersion = rawVersion;
        goPackagePath = "github.com/${owner}/${repo}";
        src =
          let path = traceValFn (path: "Using local ${repo} sources from ${path}\n") config.status_go.src_override;
          in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
            inherit path;
            name = "${repo}-source-${shortRev}";
            filter =
              # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
              mkFilter {
                dirRootsToInclude = [];
                dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".vscode" ".dependabot" ".github" ".ethereumtest" "build" ];
                filesToInclude = [ "Makefile" "go.mod" "go.sum" "VERSION" ];
                root = path;
              };
          };
    } else
      # Otherwise grab it from the location defined by status-go-version.json
      let
        versionJSON = importJSON ../../status-go-version.json; # TODO: Simplify this path search with lib.locateDominatingFile
        sha256 = versionJSON.src-sha256;
      in rec {
        inherit (versionJSON) owner repo version;
        rev = versionJSON.commit-sha1;
        shortRev = strings.substring 0 7 rev;
        rawVersion = versionJSON.version;
        cleanVersion = utils.sanitizeVersion versionJSON.version;
        goPackagePath = "github.com/${owner}/${repo}";
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
    Version = srcData.cleanVersion;
  };
  # These are necessary for status-go to show correct version
  paramsLdFlags = attrValues (mapAttrs (name: value:
    "-X github.com/status-im/status-go/params.${name}=${value}"
  ) goBuildParams);

  goBuildLdFlags = paramsLdFlags ++ [
    "-s" # -s disabled symbol table
    "-w" # -w disables DWARF debugging information
  ];

  statusGoArgs = { inherit (srcData) src owner repo rev cleanVersion goPackagePath; inherit goBuildFlags goBuildLdFlags; };
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

  android = rec {
    buildInputs = [ status-go-packages.android ];
    shell = mkShell {
      inherit buildInputs;
      shellHook = ''
        # These variables are used by the Status Android Gradle build script in android/build.gradle
        export STATUS_GO_ANDROID_LIBDIR=${status-go-packages.android}/lib
      '';
    };
  };
  ios = rec {
    buildInputs = [ status-go-packages.ios ];
    shell = mkShell {
      inherit buildInputs;
      shellHook = ''
        # These variables are used by the iOS build preparation section in nix/mobile/ios/default.nix
        export RCTSTATUS_FILEPATH=${status-go-packages.ios}/lib/Statusgo.framework
      '';
    };
  };
  desktop = rec {
    buildInputs = [ status-go-packages.desktop ];
    shell = mkShell {
      inherit buildInputs;
      shellHook = ''
        # These variables are used by the Status Desktop CMake build script in modules/react-native-status/desktop/CMakeLists.txt
        export STATUS_GO_DESKTOP_INCLUDEDIR=${status-go-packages.desktop}/include
        export STATUS_GO_DESKTOP_LIBDIR=${status-go-packages.desktop}/lib
      '';
    };
  };
  platforms = [ android ios desktop ];

in {
  shell = mergeSh mkShell {} (catAttrs "shell" platforms);

  # CHILD DERIVATIONS
  inherit android ios desktop;
}

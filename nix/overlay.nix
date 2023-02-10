# Override some packages and utilities in 'pkgs'
# and make them available globally via callPackage.
#
# For more details see:
# - https://nixos.wiki/wiki/Overlays
# - https://nixos.org/nixos/nix-pills/callpackage-design-pattern.html

self: super:

let
  inherit (super) stdenv stdenvNoCC callPackage;
  lib = (super.lib or { }) // (import ./lib {
    inherit (super) lib;
    inherit (self) config;
  });

  gomobileSrcOverride = "/Users/vvlasov/c/mobile";
  # Warning message about using local sources
  localSrcWarn = (path: "Using local gomobile sources from ${path}");
  localGomobileSrc = rec {
    owner = "status-im";
    repo = "mobile";
    rev = "unknown";
    shortRev = rev;
    rawVersion = "develop";
    cleanVersion = rawVersion;
    goPackagePath = "github.com/${owner}/${repo}";
    # We use builtins.path so that we can name the resulting derivation,
    # Normally the name would not be deterministic, taken from the checkout directory.
    src = builtins.path rec {
      path = lib.traceValFn localSrcWarn gomobileSrcOverride;
      name = "${repo}-source-${shortRev}";
      # Keep this filter as restrictive as possible in order
      # to avoid unnecessary rebuilds and limit closure size
      filter = lib.mkFilter {
        root = path;
        include = [ ".*" ];
        exclude = [
          ".*/[.]git.*" ".*[.]md" ".*[.]yml" ".*/.*_test.go$"
          "VERSION" "_assets/.*" "build/.*"
          ".*/.*LICENSE.*" ".*/CONTRIB.*" ".*/AUTHOR.*"
        ];
      };
    };
  };
in {
  inherit lib;
  # Fix for MacOS
  mkShell = super.mkShell.override { stdenv = stdenvNoCC; };

  # Various utilities
  utils = callPackage ./tools/utils.nix { };
  # Project dependencies
  deps = {
    clojure = callPackage ./deps/clojure { };
    gradle = callPackage ./deps/gradle { };
    nodejs = callPackage ./deps/nodejs { };
    nodejs-patched = callPackage ./deps/nodejs-patched { };
    react-native = callPackage ./deps/react-native { };
  };

  # For parsing gradle.properties into an attrset
  gradlePropParser = callPackage ./tools/gradlePropParser.nix { };

  # Package version adjustments
  gradle = super.gradle_5;
  nodejs = super.nodejs-16_x;
  yarn = super.yarn.override { nodejs = super.nodejs-16_x; };
  openjdk = super.openjdk8_headless;
  xcodeWrapper = callPackage ./pkgs/xcodeenv/compose-xcodewrapper.nix { } {
    version = "13.3";
    allowHigher = true;
  };
  go = super.go_1_18;
  buildGoPackage = super.buildGo118Package;
  buildGoModule = super.buildGo118Module;
  gomobile = (super.gomobile.overrideAttrs (old: {
    src = "/Users/vvlasov/c/mobile";
    vendorSha256 = "sha256-TZ/Yhe8gMRQUZFAs9G5/cf2b9QGtTHRSObBFD5Pbh7Y=";
    # patches = self.fetchurl { # https://github.com/golang/mobile/pull/84
    #   url = "https://github.com/golang/mobile/commit/f20e966e05b8f7e06bed500fa0da81cf6ebca307.patch";
    #   sha256 = "sha256-TZ/Yhe8gMRQUZFAs9G5/cf2b9QGtTHRSObBFD5Pbh7Y=";
    # };
    })).override rec {
      buildGoModule = args: super.buildGoModule ( args // {
        src =  "/Users/vvlasov/c/mobile";
        vendorSha256 = "sha256-C6GD3NMolRIve1siG8wpcQav7ZWTugjce8K22EQDD7M=";
    });
    # FIXME: No Android SDK packages for aarch64-darwin.
    withAndroidPkgs = stdenv.system != "aarch64-darwin";
    androidPkgs = self.androidEnvCustom.compose;
  };

  # Android environement
  androidEnvCustom = callPackage ./pkgs/android-sdk { };
  androidPkgs = self.androidEnvCustom.pkgs;
  androidShell = self.androidEnvCustom.shell;

  # Custom packages
  aapt2 = callPackage ./pkgs/aapt2 { };
  patchMavenSources = callPackage ./pkgs/patch-maven-srcs { };
  goMavenResolver = callPackage ./pkgs/go-maven-resolver { };
}

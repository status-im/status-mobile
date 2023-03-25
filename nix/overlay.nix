# Override some packages and utilities in 'pkgs'
# and make them available globally via callPackage.
#
# For more details see:
# - https://nixos.wiki/wiki/Overlays
# - https://nixos.org/nixos/nix-pills/callpackage-design-pattern.html

self: super:

let
  inherit (super) stdenv stdenvNoCC callPackage;
in {
  # Fix for MacOS
  mkShell = super.mkShell.override { stdenv = stdenvNoCC; };

  # Various utilities
  utils = callPackage ./tools/utils.nix { };
  lib = (super.lib or { }) // (import ./lib {
    inherit (super) lib;
    inherit (self) config;
  });

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

  # Clojure formatting tool
  zprint = super.zprint.override rec {
    buildGraalvmNativeImage = args: super.buildGraalvmNativeImage ( args // rec {
      inherit (args) pname;
      version = "1.2.5";
      src = self.fetchurl {
        url = "https://github.com/kkinnear/${pname}/releases/download/${version}/${pname}-filter-${version}";
        sha256 = "sha256-PWdR5jqyzvTk9HoxqDldwtZNik34dmebBtZZ5vtva4A=";
      };
    });
  };

  # Package version adjustments
  gradle = super.gradle_6;
  nodejs = super.nodejs-18_x;
  yarn = super.yarn.override { nodejs = super.nodejs-18_x; };
  openjdk = super.openjdk8_headless;
  xcodeWrapper = callPackage ./pkgs/xcodeenv/compose-xcodewrapper.nix { } {
    version = "13.3";
    allowHigher = true;
  };
  go = super.go_1_18;
  buildGoPackage = super.buildGo118Package;
  buildGoModule = super.buildGo118Module;
  gomobile = (super.gomobile.overrideAttrs (old: {
    patches = self.fetchurl { # https://github.com/golang/mobile/pull/84
      url = "https://github.com/golang/mobile/commit/f20e966e05b8f7e06bed500fa0da81cf6ebca307.patch";
      sha256 = "sha256-TZ/Yhe8gMRQUZFAs9G5/cf2b9QGtTHRSObBFD5Pbh7Y=";
    };
  })).override {
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

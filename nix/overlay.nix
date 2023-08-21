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

  lib = (super.lib or { }) // (import ./lib {
    inherit (super) lib;
  });

  # Project dependencies
  deps = {
    clojure = callPackage ./deps/clojure { };
    gradle = callPackage ./deps/gradle { };
    nodejs = callPackage ./deps/nodejs { };
    nodejs-patched = callPackage ./deps/nodejs-patched { };
    react-native = callPackage ./deps/react-native { };
  };

  # Fix for missing libarclite_macosx.a in Xcode 14.3.
  # https://github.com/ios-control/ios-deploy/issues/580
  ios-deploy = super.darwin.ios-deploy.overrideAttrs (old: rec {
    version = "1.12.2";
    src = super.fetchFromGitHub {
      owner = "ios-control";
      repo = "ios-deploy";
      rev = version;
      sha256 = "sha256-TVGC+f+1ow3b93CK3PhIL70le5SZxxb2ug5OkIg8XCA";
    };
  });

  # Checks fail on darwin.
  git-lfs = super.git-lfs.overrideAttrs (old: {
    doCheck = false;
  });

  # Downgrade watchman in attempt to fix "too many files open issue"
  watchman = callPackage ./pkgs/watchman {
    inherit (super.darwin.apple_sdk.frameworks) CoreServices;
    autoconf = super.buildPackages.autoconf269;
  };

  # Package version adjustments
  gradle = super.gradle_8;
  nodejs = super.nodejs-18_x;
  yarn = super.yarn.override { nodejs = super.nodejs-18_x; };
  openjdk = super.openjdk11_headless;
  xcodeWrapper = super.xcodeenv.composeXcodeWrapper {
    version = "14.0";
    allowHigher = true;
  };
  go = super.go_1_19;
  buildGoPackage = super.buildGo119Package;
  buildGoModule = super.buildGo119Module;
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

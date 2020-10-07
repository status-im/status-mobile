# Override some packages and utilities in 'pkgs'
# and make them available globally via callPackage.
#
# For more details see:
# - https://nixos.wiki/wiki/Overlays
# - https://nixos.org/nixos/nix-pills/callpackage-design-pattern.html

self: super:

let inherit (super) stdenv stdenvNoCC callPackage;
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

  # Package version adjustments
  go = super.pkgs.go_1_14;
  gradle = super.pkgs.gradle_5;
  nodejs = super.pkgs.nodejs-12_x;
  openjdk = super.pkgs.openjdk8_headless;
  xcodeWrapper = callPackage ./pkgs/xcodeenv/compose-xcodewrapper.nix { } {
    version = "11.5";
    allowHigher = true;
  };

  # Nim patch
  patchedNim = super.pkgs.nim.overrideAttrs (_: { 
    version = "pr-15268"; 
    patches = [ (super.pkgs.fetchurl { 
      url = "https://patch-diff.githubusercontent.com/raw/nim-lang/Nim/pull/15268.patch"; 
      sha256 = "04jg0ngqlyzwrc19rafx4s257my6bhd27myv4p78hmsyzhp36za6";
    })]; 
  });


  # Android environement
  androidEnvCustom = callPackage ./pkgs/android-sdk { };
  androidPkgs = self.androidEnvCustom.licensedPkgs;
  androidShell = self.androidEnvCustom.shell;

  # Custom packages
  aapt2 = callPackage ./pkgs/aapt2 { };
  gomobile = callPackage ./pkgs/gomobile { };
  patchMavenSources = callPackage ./pkgs/patch-maven-srcs { };
  goMavenResolver = callPackage ./pkgs/go-maven-resolver { };
}

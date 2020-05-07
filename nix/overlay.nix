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
    react-native = callPackage ./deps/react-native { };
  };

  # For patching Node.js modules with Gradle repo path
  patchNodeModules = callPackage ./tools/patchNodeModules.nix { };

  # Package version adjustments
  xcodeWrapper = super.xcodeenv.composeXcodeWrapper { version = "11.4.1"; };
  openjdk = super.pkgs.openjdk8_headless;
  nodejs = super.pkgs.nodejs-12_x;

  # Android environement
  androidEnvCustom = callPackage ./pkgs/android-sdk { };
  androidPkgs = self.androidEnvCustom.licensedPkgs;
  androidShell = self.androidEnvCustom.shell;

  # Custom packages
  aapt2 = callPackage ./pkgs/aapt2 { };
  gomobile = callPackage ./pkgs/gomobile { };
  qt5custom = callPackage ./pkgs/qt5custom { };
  qtkeychain-src = callPackage ./pkgs/qtkeychain-src { };
  appimagekit = callPackage ./pkgs/appimagekit { };
  linuxdeployqt = callPackage ./pkgs/linuxdeployqt { inherit (self) appimagekit; };
  patchMavenSources = callPackage ./pkgs/patch-maven-srcs { };
}

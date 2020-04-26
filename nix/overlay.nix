#
# Override some packages and utilities in 'pkgs'
# and make them available globally via callPackage.
#
# For more details see:
# - https://nixos.wiki/wiki/Overlays
# - https://nixos.org/nixos/nix-pills/callpackage-design-pattern.html
#

self: super:

let
  inherit (super) stdenv stdenvNoCC callPackage;
in {
  # Fix for MacOS
  mkShell = super.mkShell.override { stdenv = stdenvNoCC; };

  # Various utilities
  utils = callPackage ./tools/utils.nix { };
  lib = (super.lib or {}) // {
    mkFilter = callPackage ./tools/mkFilter.nix { };
    mergeSh = callPackage ./tools/mergeSh.nix { };
  };

  # Android environement
  androidEnvCustom = callPackage ./mobile/android/sdk { };
  androidPkgs = self.androidEnvCustom.licensedPkgs;
  androidShell = self.androidEnvCustom.shell;

  # Package version adjustments
  xcodeWrapper = super.xcodeenv.composeXcodeWrapper { version = "11.4.1"; };
  openjdk = super.pkgs.openjdk8_headless;
  nodejs = super.pkgs.nodejs-12_x;

  # Custom packages
  gomobile = callPackage ./pkgs/gomobile { };
}

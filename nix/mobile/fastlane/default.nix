{ lib, stdenv, callPackage, mkShell,
  bundlerEnv, cocoapods, bundler, ruby, curl }:

let
  inherit (lib) optionals optionalString unique;

  fastlane = callPackage ../../../fastlane {
    bundlerEnv = _:
      bundlerEnv {
        name = "fastlane-gems";
        gemdir = ../../../fastlane;
      };
  };

  inherit (fastlane) shellHook;

  buildInputs = [ ruby bundler fastlane curl ]
    ++ optionals stdenv.isDarwin [ cocoapods ];
in {
  # HELPERS
  inherit shellHook buildInputs;

  # TARGETS
  drv = fastlane;
  shell = mkShell {
    inherit shellHook buildInputs;
  };
}

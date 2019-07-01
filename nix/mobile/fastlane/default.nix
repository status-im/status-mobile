{ stdenv, target-os, callPackage, mkShell, makeWrapper,
  bundlerEnv, bundler, ruby, curl }:

let
  platform = callPackage ../../platform.nix { inherit target-os; };
  shellBootstraper = callPackage ../../shell-bootstrap.nix { };
  useFastlanePkg = platform.targetAndroid && !stdenv.isDarwin;
  fastlane = callPackage ../../../fastlane {
    bundlerEnv = _: bundlerEnv { 
      name = "fastlane-gems";
      gemdir = ../../../fastlane;
    };
  };
  buildInputs = if useFastlanePkg then [ fastlane curl ] else stdenv.lib.optionals platform.targetMobile [ bundler ruby ]; # bundler/ruby used for fastlane on macOS
  shellHook = stdenv.lib.optionalString useFastlanePkg fastlane.shellHook;

  # TARGETS
  shell = mkShell (shellBootstraper {
    inherit buildInputs shellHook;
  });

in {
  inherit buildInputs shellHook;

  # TARGETS
  inherit shell;
}

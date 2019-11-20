{ stdenv, target-os, callPackage, mkShell, makeWrapper,
  bundlerEnv, bundler, ruby, curl }:

let
  inherit (stdenv.lib) optionals optionalString unique;

  platform = callPackage ../../platform.nix { inherit target-os; };
  fastlane = callPackage ../../../fastlane {
    bundlerEnv = _:
      bundlerEnv {
        name = "fastlane-gems";
        gemdir = ../../../fastlane;
      };
  };
  bundlerDeps = optionals platform.targetMobile [
    bundler
    ruby
  ]; # bundler/ruby used for fastlane on macOS
  inherit (fastlane) shellHook;

  # TARGETS
  shell = mkShell {
    buildInputs = [ fastlane curl ] ++ bundlerDeps;
    inherit shellHook;
  };

in {
  # We only include bundler in regular shell if targetting iOS, because that's how the CI builds the whole project
  buildInputs = unique (optionals platform.targetIOS bundlerDeps);
  inherit shellHook;

  # TARGETS
  inherit shell;
}

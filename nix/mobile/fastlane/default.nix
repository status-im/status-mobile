{ stdenv, target-os, callPackage, mkShell, makeWrapper,
  bundlerEnv, bundler, ruby, curl }:

let
  inherit (stdenv.lib) optionals optionalString unique;

  platform = callPackage ../../platform.nix { inherit target-os; };
  useFastlanePkg = platform.targetAndroid && !stdenv.isDarwin;
  fastlane = callPackage ../../../fastlane {
    bundlerEnv = _: bundlerEnv { 
      name = "fastlane-gems";
      gemdir = ../../../fastlane;
    };
  };
  bundlerDeps = optionals platform.targetMobile [ bundler ruby ];
  shellHook = optionalString useFastlanePkg fastlane.shellHook;

  # TARGETS
  shell = mkShell {
    buildInputs = if useFastlanePkg then [ fastlane curl ] else bundlerDeps; # bundler/ruby used for fastlane on macOS
    inherit shellHook;
  };

in {
  # We only include bundler in regular shell if targetting iOS, because that's how the CI builds the whole project 
  buildInputs = unique (optionals (!useFastlanePkg && platform.targetIOS) bundlerDeps);
  inherit shellHook;

  # TARGETS
  inherit shell;
}

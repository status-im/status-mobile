{ system ? builtins.currentSystem
, config ? { android_sdk.accept_license = true; }, overlays ? []
, pkgs ? (import <nixpkgs> { inherit system config overlays; })
, target-os }:

let
  platform = pkgs.callPackage ./nix/platform.nix { inherit target-os; };
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  stdenv' = pkgs.stdenvNoCC;
  gradle = pkgs.gradle_4_10;
  statusDesktop = pkgs.callPackage ./nix/desktop { inherit target-os status-go pkgs; inherit (pkgs) darwin; stdenv = stdenv'; nodejs = nodejs'; };
  statusMobile = pkgs.callPackage ./nix/mobile { inherit target-os config pkgs status-go gradle; inherit (pkgs.xcodeenv) composeXcodeWrapper; stdenv = stdenv'; nodejs = nodejs'; };
  status-go = pkgs.callPackage ./nix/status-go { inherit target-os; inherit (pkgs.xcodeenv) composeXcodeWrapper; inherit (statusMobile) xcodewrapperArgs; androidPkgs = statusMobile.androidComposition; };
  nodejs' = pkgs.nodejs-10_x;
  yarn' = pkgs.yarn.override { nodejs = nodejs'; };
  nodePkgBuildInputs = [
    nodejs'
    pkgs.python27 # for e.g. gyp
    yarn'
  ];
  selectedSources =
    stdenv'.lib.optional platform.targetDesktop statusDesktop ++
    stdenv'.lib.optional platform.targetMobile statusMobile;

in with stdenv'; stdenv'.mkDerivation rec {
  name = "status-react-build-env";

  buildInputs = with pkgs; [
    clojure
    leiningen
    maven
    watchman
  ] ++ nodePkgBuildInputs
    ++ lib.optional isDarwin cocoapods
    ++ lib.optional (isDarwin && !platform.targetIOS) clang
    ++ lib.optional (!isDarwin) gcc7
    ++ lib.catAttrs "buildInputs" selectedSources;
  shellHook = lib.concatStrings (lib.catAttrs "shellHook" selectedSources);
}

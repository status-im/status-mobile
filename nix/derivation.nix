{ system ? builtins.currentSystem
, config ? { android_sdk.accept_license = true; }, overlays ? []
, pkgs ? (import <nixpkgs> { inherit system config overlays; })
, target-os }:

let
  platform = pkgs.callPackage ./platform.nix { inherit target-os; };
  shellBootstraper = pkgs.callPackage ./shell-bootstrap.nix { };
  # TODO: Try to use stdenv for iOS. The problem is with building iOS as the build is trying to pass parameters to Apple's ld that are meant for GNU's ld (e.g. -dynamiclib)
  stdenv = pkgs.stdenvNoCC;
  maven = pkgs.maven;
  baseGo = pkgs.go_1_11;
  go = pkgs.callPackage ./go { inherit baseGo; };
  buildGoPackage = pkgs.buildGoPackage.override { inherit go; };
  desktop = pkgs.callPackage ./desktop { inherit target-os stdenv status-go pkgs nodejs; inherit (pkgs) darwin; go = baseGo; };
  mobile = pkgs.callPackage ./mobile { inherit target-os config stdenv pkgs nodejs yarn status-go maven localMavenRepoBuilder mkFilter prod-build-fn; inherit (pkgs.xcodeenv) composeXcodeWrapper; };
  status-go = pkgs.callPackage ./status-go { inherit target-os go buildGoPackage; inherit (mobile.ios) xcodeWrapper; androidPkgs = mobile.android.androidComposition; };
  mkFilter = import ./tools/mkFilter.nix { inherit (stdenv) lib; };
  localMavenRepoBuilder = pkgs.callPackage ./tools/maven/maven-repo-builder.nix { inherit (pkgs) stdenv; };
  prod-build-fn = pkgs.callPackage ./targets/prod-build.nix { inherit stdenv pkgs target-os nodejs localMavenRepoBuilder mkFilter; };
  nodejs = pkgs.nodejs-10_x;
  yarn = pkgs.yarn.override { inherit nodejs; };
  nodePkgBuildInputs = [
    nodejs
    pkgs.nodePackages_10_x.react-native-cli
    pkgs.python27 # for e.g. gyp
    yarn
  ];
  selectedSources =
    stdenv.lib.optional platform.targetDesktop desktop ++
    stdenv.lib.optional platform.targetMobile mobile;

  # TARGETS
  leiningen-shell = pkgs.mkShell (shellBootstraper {
    buildInputs = with pkgs; [ clojure leiningen maven nodejs ];
    shellHook =
      if target-os == "android" then mobile.android.shellHook else
      if target-os == "ios" then mobile.ios.shellHook else "";
  });
  watchman-shell = pkgs.mkShell {
    buildInputs = with pkgs; [ watchman ];
  };

in {
  # CHILD DERIVATIONS
  inherit mobile;

  # TARGETS
  leiningen = {
    shell = leiningen-shell;
  };
  watchman = {
    shell = watchman-shell;
  };

  shell = with stdenv; {
    buildInputs = with pkgs;
      nodePkgBuildInputs
      ++ lib.optional isDarwin cocoapods
      ++ lib.optional (isDarwin && !platform.targetIOS) clang
      ++ lib.optional (!isDarwin) gcc8
      ++ lib.catAttrs "buildInputs" selectedSources;
    shellHook = lib.concatStrings (lib.catAttrs "shellHook" selectedSources);
  };
}

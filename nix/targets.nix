{ 
  config ? {},
  pkgs ? import ./pkgs.nix { inherit config; }
}:

with pkgs;
let
  localMavenRepoBuilder = callPackage ./tools/maven/maven-repo-builder.nix {
    inherit stdenv;
  };

  status-go = callPackage ./status-go { 
    inherit (mobile) xcodeWrapper;
    androidPkgs = mobile.android.androidComposition;
  };

  desktop = callPackage ./desktop {
    inherit darwin;
    status-go = status-go.desktop;
  };

  mobile = callPackage ./mobile {
    inherit status-go localMavenRepoBuilder;
    inherit (xcodeenv) composeXcodeWrapper;
  };
in {
  inherit mobile desktop status-go;
}

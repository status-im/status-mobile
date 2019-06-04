# target-os = [ 'windows' 'linux' 'macos' 'darwin' 'android' 'ios' 'all' ]
{ config ? { android_sdk.accept_license = true; },
  nixpkgs-bootstrap ? import ./nix/nixpkgs-bootstrap.nix { inherit config; },
  pkgs ? nixpkgs-bootstrap.pkgs,
  stdenv ? pkgs.stdenv,
  target-os ? "all" }:

let deriv = pkgs.callPackage ./nix/derivation.nix { inherit pkgs target-os; inherit (nixpkgs-bootstrap) config; };

in {
  targets = {
    inherit (deriv) mobile leiningen watchman;
  };

  inherit (deriv) shell;
}

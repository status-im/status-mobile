# target-os = [ 'windows' 'linux' 'macos' 'darwin' 'android' 'ios' 'all' ]
{ config ? null,
  nixpkgs-bootstrap ? import ./nix/nixpkgs-bootstrap.nix { inherit config; },
  pkgs ? nixpkgs-bootstrap.pkgs,
  stdenv ? pkgs.stdenv,
  target-os ? "all" }:

(pkgs.callPackage ./derivation.nix { inherit pkgs target-os; inherit (nixpkgs-bootstrap) config; }).overrideAttrs(_: {
  src = null;
  # TODO: Figure out if there's a better way to do this
  # NOTE: There's a weird difference in behavior between Linux and macOS: in Linux the packages will only be fetched by `nix build` if unpackPhase exists. In macOS it's the other way around :-/
  phases = with stdenv; lib.optional isLinux [ "unpackPhase" ] ++ [ "noPhase" ];
  unpackPhase = "true";
  noPhase = "mkdir -p $out";
})

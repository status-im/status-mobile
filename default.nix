# target-os = [ 'windows' 'linux' 'macos' 'darwin' 'android' 'ios' 'all' ]
{ config ? { android_sdk.accept_license = true; },
  pkgs ? import ((import <nixpkgs> { }).fetchFromGitHub {
    owner = "status-im";
    repo = "nixpkgs";
    rev = "db492b61572251c2866f6b5e6e94e9d70e7d3021";
    sha256 = "188r7gbcrxi20nj6xh9bmdf3lbjwb94v9s0wpacl7q39g1fca66h";
    name = "nixpkgs-source";
  }) { inherit config; },
  stdenv ? pkgs.stdenv,
  target-os ? "all" }:

(pkgs.callPackage ./derivation.nix { inherit pkgs target-os config stdenv; }).overrideAttrs(_: {
  src = null;
  # TODO: Figure out if there's a better way to do this
  # NOTE: There's a weird difference in behavior between Linux and macOS: in Linux the packages will only be fetched by `nix build` if unpackPhase exists. In macOS it's the other way around :-/
  phases = with stdenv; lib.optional isLinux [ "unpackPhase" ] ++ [ "noPhase" ];
  unpackPhase = "true";
  noPhase = "mkdir -p $out";
})

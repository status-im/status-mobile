{ lib, buildGoPackage, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGoPackage rec {
  pname = "go-maven-resolver";
  version = strings.substring 0 7 rev;
  owner = "status-im";
  repo = pname;
  rev = "72b6c12ab193f59d197e7f63273ec0c079b6e3a9";
  sha256 = "0r6k74716175pjy8vjz0fig2h010fabl00w114qd2wb2iwq4z3x4";
  goPackagePath = "github.com/${owner}/${repo}";

  goDeps = ./deps.nix;

  src = fetchFromGitHub {
    name = "${repo}-${version}-source";
    inherit owner repo rev sha256;
  };
}

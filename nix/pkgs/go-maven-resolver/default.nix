{ lib, buildGoPackage, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGoPackage rec {
  pname = "go-maven-resolver";
  version = strings.substring 0 7 rev;
  owner = "status-im";
  repo = pname;
  rev = "v1.0.0";
  sha256 = "1768mwk1kawrqc72s3yq7k3xbh2z8kn7sfxpxwxdr9cghzwcqcld";
  goPackagePath = "github.com/${owner}/${repo}";

  goDeps = ./deps.nix;

  src = fetchFromGitHub {
    name = "${repo}-${version}-source";
    inherit owner repo rev sha256;
  };
}

{ lib, buildGoPackage, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGoPackage rec {
  pname = "go-maven-resolver";
  version = strings.substring 0 7 rev;
  owner = "status-im";
  repo = pname;
  rev = "c6f05fc28e4a9df1dc09bca1473b20eb1eec212e";
  sha256 = "1lghzickfms8s0bm6qiq1xg1nz4qsv9k3vmz4d255acnydxmc893";
  goPackagePath = "github.com/${owner}/${repo}";

  goDeps = ./deps.nix;

  src = fetchFromGitHub {
    name = "${repo}-${version}-source";
    inherit owner repo rev sha256;
  };
}

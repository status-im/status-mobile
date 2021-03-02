{ lib, buildGo114Package, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGo114Package rec {
  pname = "go-maven-resolver";
  version = strings.substring 0 7 rev;
  owner = "status-im";
  repo = pname;
  rev = "v1.1.0";
  sha256 = "15i8bkqv0m2pq8hzjy02a44z5xg67c8xcdaj6pc8p7w1m039n6qn";
  goPackagePath = "github.com/${owner}/${repo}";

  goDeps = ./deps.nix;

  src = fetchFromGitHub {
    name = "${repo}-${version}-source";
    inherit owner repo rev sha256;
  };
}

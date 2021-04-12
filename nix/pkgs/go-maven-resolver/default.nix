{ lib, buildGo116Package, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGo116Module rec {
  pname = "go-maven-resolver";
  version = "v1.1.1";

  vendorSha256 = "1p9pl33zpbw8zc85301mnp692lkr46ppm1q99wnqwynzi7x8hnkn";

  src = fetchFromGitHub rec {
    name = "${repo}-${version}-source";
    owner = "status-im";
    repo = pname;
    rev = version;
    sha256 = "0pjab7v4cq3w5z3h0g9bfahqfs4raqp8y9sxwsri4zgbvdllq11q";
  };
}

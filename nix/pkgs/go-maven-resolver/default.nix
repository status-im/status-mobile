{ lib, buildGo116Module, fetchFromGitHub }:

let
  inherit (lib) strings;
in buildGo116Module rec {
  pname = "go-maven-resolver";
  version = "v1.1.2";

  vendorSha256 = "1p9pl33zpbw8zc85301mnp692lkr46ppm1q99wnqwynzi7x8hnkn";

  src = fetchFromGitHub rec {
    name = "${repo}-${version}-source";
    owner = "status-im";
    repo = pname;
    rev = version;
    sha256 = "0p3qz5w8spzdxs70m1sdfwi0ajv4ciw3f7fxligf45vj2fwp5dab";
  };
}

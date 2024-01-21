{ buildGoModule, fetchFromGitHub }:

buildGoModule rec {
  pname = "go-maven-resolver";
  version = "v1.1.2";

  vendorSha256 = "1p9pl33zpbw8zc85301mnp692lkr46ppm1q99wnqwynzi7x8hnkn";

  # https://github.com/status-im/go-maven-resolver/pull/7
  src = fetchFromGitHub {
    owner = "status-im";
    repo = pname;
    rev = "2196acb4fbbc3bacf8d2a5d6afb7c73f7f6bc812";
    sha256 = "sha256-GtB+DGQ3yyA8hNUBG4ZgDdT28cabk7D7nqpwtgiSCsc=";
  };
}

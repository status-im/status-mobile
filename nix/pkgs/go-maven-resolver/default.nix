{ buildGoModule, fetchFromGitHub }:

buildGoModule rec {
  pname = "go-maven-resolver";
  version = "v1.1.2";

  vendorHash = "sha256-dlqI+onfeo4tTwmHeq8heVKRzLU1gFEQ+4iv+8egN90=";

  src = fetchFromGitHub rec {
    name = "${repo}-${version}-source";
    owner = "status-im";
    repo = pname;
    rev = version;
    sha256 = "0p3qz5w8spzdxs70m1sdfwi0ajv4ciw3f7fxligf45vj2fwp5dab";
  };
}

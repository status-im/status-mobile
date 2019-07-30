#
# This Nix expression returns our sanctioned version of Gradle
#

{ gradleGen, fetchurl }:

gradleGen.gradleGen rec {
  name = "gradle-5.4.1";
  nativeVersion = "0.17";

  src = fetchurl {
    url = "http://services.gradle.org/distributions/${name}-bin.zip";
    sha256 = "1p15q25bf6zv2gch9dzd9afp1n2dshk0rh5bg253rwal9wgavnvv";
  };
}

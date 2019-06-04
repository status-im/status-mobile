#
# This Nix expression returns our sanctioned version of Gradle
#

{ gradleGen, fetchurl }:

gradleGen.gradleGen rec {
  name = "gradle-5.1.1";
  nativeVersion = "0.14";

  src = fetchurl {
    url = "http://services.gradle.org/distributions/${name}-bin.zip";
    sha256 = "16671jp5wdr3q6p91h6szkgcxg3mw9wpgp6hjygbimy50lv34ls9";
  };
}

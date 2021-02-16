{ pkgs, stdenv, lib, fetchFromGitHub
# Dependencies
, xcodeWrapper
, writeScript
, androidPkgs
, newScope
, git 
, platform ? "android"
, arch ? "386"
, api ? "23" } :


let
  callPackage = newScope {};
  src = pkgs.fetchgit {
    url = "https://github.com/status-im/nim-sqlcipher";
    rev = "99e9ed1734f39b3a79a435c091cc505b1d8c2d05";
    sha256 = "14d5mqsi60dgw7wb6ab8a7paw607axblfysf88vmj3qix5z571wg";
    fetchSubmodules = false;
  };

  flags = callPackage ./getFlags.nix {platform = platform; arch = arch;};
  sqlcipher = callPackage ./sqlcipher.nix {platform = platform; arch = arch;};
  openssl = callPackage ./openssl.nix {platform = platform; arch = arch;};
in 
  stdenv.mkDerivation rec {
  name = "nim-sqlcipher_lib";
  inherit src;
  #buildInputs = with pkgs; [ perl ];

  phases = ["unpackPhase" "buildPhase" "installPhase"];

  buildPhase = ''
    ${flags.vars}
  	echo -e "SQLCipher static library"
    echo ${sqlcipher}
	  mkdir -p lib
    $CC \
      -DSQLITE_HAS_CODEC -DSQLITE_TEMP_STORE=3 \
      -I${openssl}/include -pthread ${flags.compiler}	\
      ${sqlcipher}/sqlite3.c \
      -c \
      -o lib/sqlcipher.o
    $AR rcs lib/libsqlcipher.a lib/sqlcipher.o
  '';

  installPhase = ''
    mkdir $out
    cp lib/libsqlcipher.a $out
  '';
}

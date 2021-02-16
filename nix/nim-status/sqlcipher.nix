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
    url = "https://github.com/sqlcipher/sqlcipher";
    rev = "50376d07a5919f1777ac983921facf0bf0fc1976";
    sha256 = "0zhww6fpnfflnzp6091npz38ab6cpq75v3ghqvcj5kqg09vqm5na";
    fetchSubmodules = false;
  };

  flags = callPackage ./getFlags.nix {platform = platform; arch = arch;};
  openssl = callPackage ./openssl.nix {platform = platform; arch = arch;};

  lpthreadFlags = if flags.isIOS then "-lpthread" else "";

in 
  stdenv.mkDerivation rec {
  name = "sqlcipher_lib";
  inherit src;
  buildInputs = with pkgs; [ tcl ];

  phases = ["unpackPhase" "configurePhase" "buildPhase" "installPhase"];

  configurePhase = ''
    ${flags.vars}
    echo -e "SQLCipher's SQLite C amalgamation"
    echo "openssl"
    echo "-L${openssl}/lib ${openssl}/lib/libcrypto.a ${flags.linker}"
    ./configure --with-sysroot=${flags.isysroot} --host=${flags.host} \
        CFLAGS="-DSQLITE_HAS_CODEC -DSQLITE_TEMP_STORE=3 -I${openssl}/include -pthread ${flags.compiler}" \
        LDFLAGS="-L${openssl}/lib ${openssl}/lib/libcrypto.a ${lpthreadFlags} ${flags.linker}"
  '';

  buildPhase = ''
    make sqlite3.c
  '';

  installPhase = ''
    mkdir $out
    cp sqlite3.h $out
    cp sqlite3.c $out
  '';
}


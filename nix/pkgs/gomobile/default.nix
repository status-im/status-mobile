{ stdenv, lib, utils, callPackage, fetchgit, buildGo114Package,
  ncurses5, zlib, makeWrapper, patchelf, androidPkgs, xcodeWrapper
}:

let
  inherit (stdenv) isDarwin;
  inherit (lib) optional optionalString strings;
in buildGo114Package rec {
  pname = "gomobile";
  version = "20200622-${strings.substring 0 7 rev}";
  # WARNING: Next commit removes support for ARM 32 bit builds for iOS
  rev = "33b80540585f2b31e503da24d6b2a02de3c53ff5";
  sha256 = "0c9map2vrv34wmaycsv71k4day3b0z5p16yzxmlp8amvqb38zwlm";

  goPackagePath = "golang.org/x/mobile";
  subPackages = [ "bind" "cmd/gobind" "cmd/gomobile" ];
  goDeps = ./deps.nix;

  buildInputs = [ makeWrapper ]
    ++ optional isDarwin xcodeWrapper;

  # Ensure XCode and the iPhone SDK are present, instead of failing at the end of the build
  preConfigure = optionalString isDarwin utils.enforceiPhoneSDKAvailable;

  patches = [ ./resolve-nix-android-sdk.patch ];

  postPatch = ''
    substituteInPlace cmd/gomobile/install.go --replace "\`adb\`" "\`${androidPkgs}/bin/adb\`"
    
    # Prevent a non-deterministic temporary directory from polluting the resulting object files
    substituteInPlace cmd/gomobile/env.go \
      --replace \
        'tmpdir, err = ioutil.TempDir("", "gomobile-work-")' \
        "tmpdir = filepath.Join(os.Getenv(\"NIX_BUILD_TOP\"), \"gomobile-work\")" \
      --replace '"io/ioutil"' ""
    substituteInPlace cmd/gomobile/init.go \
      --replace \
        'tmpdir, err = ioutil.TempDir(gomobilepath, "work-")' \
        "tmpdir = filepath.Join(os.Getenv(\"NIX_BUILD_TOP\"), \"work\")"
  '';

  preBuild = ''
    mkdir $NIX_BUILD_TOP/gomobile-work $NIX_BUILD_TOP/work
  '';

  # Necessary for GOPATH when using gomobile.
  postInstall = ''
    echo "Creating $out"
    mkdir -p $out/src/$goPackagePath
    echo "Copying from $src"
    cp -a $src/. $out/src/$goPackagePath
  '';

  src = fetchgit {
    inherit rev sha256;
    name = "gomobile";
    url = "https://go.googlesource.com/mobile";
  };

  meta = with lib; {
    description = "A tool for building and running mobile apps written in Go.";
    longDescription = "Gomobile is a tool for building and running mobile apps written in Go.";
    homepage = https://go.googlesource.com/mobile;
    license = licenses.bsdOriginal;
    maintainers = with maintainers; [ sheenobu pombeirp ];
    platforms = with platforms; linux ++ darwin;
  };
}

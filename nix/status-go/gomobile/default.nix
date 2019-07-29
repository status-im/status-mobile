{ stdenv, target-os, callPackage, utils, fetchgit,
  buildGoPackage, glibc, ncurses5, zlib, makeWrapper, patchelf,
  platform-tools, xcodeWrapper
}:

let
  inherit (stdenv) isDarwin;
  inherit (stdenv.lib) optional optionalString strings;

  platform = callPackage ../../platform.nix { inherit target-os; };

in buildGoPackage rec {
  pname = "gomobile";
  version = "20190319-${strings.substring 0 7 rev}";
  rev = "167ebed0ec6dd457a6b24a4f61db913f0af11f70";
  sha256 = "0lspdhikhnhbwv8v0q6fs3a0pd9sjnhkpg8z03m2dc5h6f84m38w";

  goPackagePath = "golang.org/x/mobile";
  subPackages = [ "bind" "cmd/gobind" "cmd/gomobile" ];

  buildInputs = [ makeWrapper ]
    ++ optional isDarwin xcodeWrapper;

  # Ensure XCode and the iPhone SDK are present, instead of failing at the end of the build
  preConfigure = optionalString isDarwin utils.enforceiPhoneSDKAvailable;

  patches = [ ./ndk-search-path.patch ./resolve-nix-android-sdk.patch ]
    ++ optional isDarwin ./ignore-nullability-error-on-ios.patch;

  postPatch = ''
    ${optionalString platform.targetAndroid ''substituteInPlace cmd/gomobile/install.go --replace "\`adb\`" "\`${platform-tools}/bin/adb\`"''}
    
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

    echo "Creating $dev"
    mkdir -p $dev/src/$goPackagePath
    echo "Copying from $src"
    cp -a $src/. $dev/src/$goPackagePath
  '';

  preBuild = ''
    mkdir $NIX_BUILD_TOP/gomobile-work $NIX_BUILD_TOP/work
  '';

  postInstall =
    let
      inherit (stdenv.lib) makeBinPath makeLibraryPath;
    in ''
    mkdir -p $out $bin/lib

    ln -s ${ncurses5}/lib/libncursesw.so.5 $bin/lib/libtinfo.so.5
    ${if isDarwin then ''
    wrapProgram $bin/bin/gomobile \
      --prefix "PATH" : "${makeBinPath [ xcodeWrapper ]}" \
      --prefix "LD_LIBRARY_PATH" : "${makeLibraryPath [ ncurses5 zlib ]}:$bin/lib"
  '' else ''
    wrapProgram $bin/bin/gomobile \
      --prefix "LD_LIBRARY_PATH" : "${makeLibraryPath [ ncurses5 zlib ]}:$bin/lib"
  ''}
    $bin/bin/gomobile init
  '';

  src = fetchgit {
    inherit rev sha256;
    url = "https://go.googlesource.com/mobile";
  };

  outputs = [ "bin" "dev" "out" ];

  meta = with stdenv.lib; {
    description = "A tool for building and running mobile apps written in Go.";
    longDescription = "Gomobile is a tool for building and running mobile apps written in Go.";
    homepage = https://go.googlesource.com/mobile;
    license = licenses.bsdOriginal;
    maintainers = with maintainers; [ sheenobu pombeirp ];
    platforms = with platforms; linux ++ darwin;
  };
}

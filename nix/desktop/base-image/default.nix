{ stdenv, fetchurl, unzip }:

let
  defaultPackageSource = {
    version = "20190923";
    hostSystem = "x86_64-linux";
  };
  packageSources = {
    "linux" = defaultPackageSource // {
      sha256 = "1l2rmqc2mxlz4qp5pnl1763pzqh6y3aawxrd9336q5w35xgrgmcm";
    };
    "macos" = defaultPackageSource // {
      sha256 = "1j73l7xry0xw922zxhbsai1391a4i15rni1pfszr3cis8v95n21v";
      hostSystem = "x86_64-darwin";
    };
    "windows" = defaultPackageSource // {
      sha256 = "0sb1nqwy2ap7lr1vrk497fqrjhv7c470pm0kmrvwn4nas4gm40g5";
    };
  };
  packageFactory = target-os:
    let packageSource = packageSources."${target-os}";
    in stdenv.mkDerivation rec {
      inherit (packageSource) version;
      pname = "status-im-${target-os}-desktop-files";

      src = assert stdenv.lib.asserts.assertMsg
                    (stdenv.hostPlatform.system == packageSource.hostSystem)
                    "${pname} is not supported on ${stdenv.hostPlatform.system}";
        fetchurl {
          inherit (packageSource) sha256;
          url = "https://desktop-app-files.ams3.digitaloceanspaces.com/status-im-desktop-files-${target-os}-${packageSource.version}.zip";
        };

      nativeBuildInputs = [ unzip ];

      phases = [ "unpackPhase" ];
      unpackPhase = ''
        mkdir -p $out/src
        unzip $src -d $out/src
      '';

      meta = with stdenv.lib; {
        description = "A base image for Status Desktop release distributions";
        homepage = https://desktop-app-files.ams3.digitaloceanspaces.com/;
        license = licenses.gpl3;
        maintainers = [ maintainers.pombeirp ];
        platforms = platforms.linux ++ platforms.darwin;
      };
    };

in target-os:
  let package = (packageFactory target-os);
  in package // {
    shellHook = ''
      ${package.shellHook or ""}
      export STATUSREACT_${stdenv.lib.toUpper target-os}_BASEIMAGE_PATH="${package}/src"
    '';
}

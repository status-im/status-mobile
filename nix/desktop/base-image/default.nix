{ stdenv, fetchurl, unzip }:

let
  defaultPackageSource = {
    version = "20191002";
    hostSystem = "x86_64-linux";
  };
  packageSources = {
    "linux" = defaultPackageSource // {
      sha256 = "1xqa8k00kgld82d3knfbwn90nsw2f7s8h8r8188q966fk99m4g0h";
    };
    "macos" = defaultPackageSource // {
      sha256 = "0nmv3agaipdlhl38wh58bgyb8pdc454gxxzig9x0sw5zp9jsaziq";
      hostSystem = "x86_64-darwin";
    };
    "windows" = defaultPackageSource // {
      sha256 = "0p6amqz5942100zm3szwbksp2rp08ybfmgiz4bmviggg8391i0zr";
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

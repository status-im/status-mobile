{ stdenv, utils, callPackage, buildGoPackage, go, xcodeWrapper }:

{ owner, repo, rev, cleanVersion, goPackagePath, src, host,
  # desktop-only arguments
  goBuildFlags, goBuildLdFlags,
  outputFileName,
  hostSystem } @ args':

let
  buildStatusGo = callPackage ./build-status-go.nix {
    inherit buildGoPackage go xcodeWrapper utils;
  };

  # Remove desktop-only arguments from args
  args = removeAttrs args' [
    "goBuildFlags" "goBuildLdFlags" "outputFileName" "hostSystem"
  ];

  buildStatusGoDesktopLib = buildStatusGo (args // {
    buildMessage = "Building desktop library";
    #GOOS=windows GOARCH=amd64 CGO_ENABLED=1 go build ${goBuildFlags} -buildmode=c-archive -o $out/${outputFileName} ./lib
    buildPhase =
      let
        CGO_LDFLAGS = stdenv.lib.concatStringsSep " " goBuildLdFlags;
      in ''
      pushd "$NIX_BUILD_TOP/go/src/${goPackagePath}" >/dev/null

      export GO111MODULE=off

      go build -o $out/${outputFileName} \
          ${goBuildFlags} \
          -buildmode=c-archive \
          -ldflags='${CGO_LDFLAGS}' \
          ./lib

      popd >/dev/null
    '';

    installPhase = ''
      mkdir -p $out/lib/${hostSystem} $out/include
      mv $out/${outputFileName} $out/lib/${hostSystem}
      mv $out/libstatus.h $out/include
    '';

    outputs = [ "out" ];

    meta = { platforms = with stdenv.lib.platforms; linux ++ darwin; };
  });

in buildStatusGoDesktopLib

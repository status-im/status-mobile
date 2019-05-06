
{ buildGoPackage, go, xcodeWrapper, pkgs, stdenv }:

{ owner, repo, rev, version, goPackagePath, src, host,

  # desktop-only arguments
  goBuildFlags, goBuildLdFlags,
  outputFileName,
  hostSystem } @ args':

with stdenv;

let
  buildStatusGo = pkgs.callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper; };

  args = removeAttrs args' [ "goBuildFlags" "goBuildLdFlags" "outputFileName" "hostSystem" ]; # Remove desktop-only arguments from args
  buildStatusGoDesktopLib = buildStatusGo (args // {
    buildMessage = "Building desktop library";
    #GOOS=windows GOARCH=amd64 CGO_ENABLED=1 go build ${goBuildFlags} -buildmode=c-archive -o $out/${outputFileName} ./lib
    buildPhase = ''
      pushd "$NIX_BUILD_TOP/go/src/${goPackagePath}" >/dev/null

      go build -o $out/${outputFileName} ${goBuildFlags} -buildmode=c-archive ${goBuildLdFlags} ./lib

      popd >/dev/null
    '';

    installPhase = ''
      mkdir -p $out/lib/${hostSystem} $out/include
      mv $out/${outputFileName} $out/lib/${hostSystem}
      mv $out/libstatus.h $out/include
    '';

    outputs = [ "out" ];

    meta = {
      platforms = with lib.platforms; linux ++ darwin;
    };
  });

in buildStatusGoDesktopLib
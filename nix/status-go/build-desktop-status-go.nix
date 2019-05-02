
{ buildGoPackage, go, xcodeWrapper, pkgs, stdenv }:

{ owner, repo, rev, version, goPackagePath, src, host,
  goBuildFlags, goBuildLdFlags,
  outputFileName,
  hostSystem } @ args':

with stdenv;

let
  args = removeAttrs args' [ "goBuildFlags" "goBuildLdFlags" "outputFileName" "hostSystem" ];
  buildStatusGo = pkgs.callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper; };

  buildStatusGoDesktopLib = buildStatusGo (args // {
    buildPhase = ''
      runHook preBuild

      runHook renameImports

      pushd "$NIX_BUILD_TOP/go/src/${goPackagePath}" >/dev/null

      echo
      echo "Building desktop library"
      echo
      #GOOS=windows GOARCH=amd64 CGO_ENABLED=1 go build ${goBuildFlags} -buildmode=c-archive -o $out/${outputFileName} ./lib
      go build -o $out/${outputFileName} ${goBuildFlags} -buildmode=c-archive ${goBuildLdFlags} ./lib

      popd >/dev/null

      runHook postBuild
    '';

    installPhase = ''
      runHook preInstall

      mkdir -p $out/lib/${hostSystem} $out/include
      mv $out/${outputFileName} $out/lib/${hostSystem}
      mv $out/libstatus.h $out/include

      runHook postInstall
    '';

    outputs = [ "out" ];

    meta = {
      platforms = with lib.platforms; linux ++ darwin;
    };
  });

in buildStatusGoDesktopLib
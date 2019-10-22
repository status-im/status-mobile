
{ stdenv, utils, callPackage,
  buildGoPackage, go, gomobile, openjdk, xcodeWrapper }:

{ owner, repo, rev, version, goPackagePath, src, host,

  # mobile-only arguments
  goBuildFlags, goBuildLdFlags,
  config } @ args':

let
  inherit (stdenv.lib) concatStringsSep makeBinPath optional;

  targetConfig = config;
  buildStatusGo = callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper utils; };

  # Remove mobile-only arguments from args
  args = removeAttrs args' [
    "config" "goBuildFlags" "goBuildLdFlags"
  ];

  buildStatusGoMobileLib = buildStatusGo (args // {
    nativeBuildInputs = [ gomobile ] ++ optional (targetConfig.name == "android") openjdk;

    buildMessage = "Building mobile library for ${targetConfig.name}";
    # Build mobile libraries
    buildPhase =
      let
        NIX_GOWORKDIR = "$NIX_BUILD_TOP/go-build";
        CGO_LDFLAGS = concatStringsSep " " goBuildLdFlags;
      in with targetConfig; ''
      mkdir ${NIX_GOWORKDIR}

      GOPATH=${gomobile.dev}:$GOPATH \
      PATH=${makeBinPath [ gomobile.bin ]}:$PATH \
      NIX_GOWORKDIR=${NIX_GOWORKDIR} \
      ${concatStringsSep " " envVars} \
      gomobile bind \
          -target=${name} \
          -ldflags='${CGO_LDFLAGS}' \
          ${concatStringsSep " " gomobileExtraFlags} \
          ${goBuildFlags} \
          -o ${outputFileName} \
          ${goPackagePath}/mobile

      rm -rf ${NIX_GOWORKDIR}
    '';

    installPhase = ''
      mkdir -p $out/lib
      mv ${targetConfig.outputFileName} $out/lib/
    '';

    outputs = [ "out" ];

    meta = {
      platforms = with stdenv.lib.platforms; linux ++ darwin;
    };
  });

in buildStatusGoMobileLib

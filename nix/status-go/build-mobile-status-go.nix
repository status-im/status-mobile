
{ stdenv, utils, callPackage,
  buildGoPackage, go, gomobile, openjdk, xcodeWrapper }:

{ owner, repo, rev, version, goPackagePath, src, host,

  # mobile-only arguments
  goBuildFlags, goBuildLdFlags,
  config } @ args':

with stdenv;

let
  targetConfig = config;
  buildStatusGo = callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper utils; };

  args = removeAttrs args' [ "config" "goBuildFlags" "goBuildLdFlags" ]; # Remove mobile-only arguments from args
  buildStatusGoMobileLib = buildStatusGo (args // {
    nativeBuildInputs = [ gomobile ] ++ lib.optional (targetConfig.name == "android") openjdk;

    buildMessage = "Building mobile library for ${targetConfig.name}";
    # Build mobile libraries
    buildPhase = ''
      mkdir $NIX_BUILD_TOP/go-build

      GOPATH=${gomobile.dev}:$GOPATH \
      PATH=${lib.makeBinPath [ gomobile.bin ]}:$PATH \
      ${lib.concatStringsSep " " targetConfig.envVars} \
      NIX_GOWORKDIR=$NIX_BUILD_TOP/go-build \
      gomobile bind ${goBuildFlags} -target=${targetConfig.name} ${lib.concatStringsSep " " targetConfig.gomobileExtraFlags} \
                    -o ${targetConfig.outputFileName} \
                    ${goBuildLdFlags} \
                    ${goPackagePath}/mobile

      rm -rf $NIX_BUILD_TOP/go-build
    '';

    installPhase = ''
      mkdir -p $out/lib
      mv ${targetConfig.outputFileName} $out/lib/
    '';

    outputs = [ "out" ];

    meta = {
      platforms = with lib.platforms; linux ++ darwin;
    };
  });

in buildStatusGoMobileLib
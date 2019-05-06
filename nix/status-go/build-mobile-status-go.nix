
{ buildGoPackage, go, gomobile, openjdk, xcodeWrapper, pkgs, stdenv }:

{ owner, repo, rev, version, goPackagePath, src, host,

  # mobile-only arguments
  goBuildFlags, goBuildLdFlags,
  config } @ args':

with stdenv;

let
  targetConfig = config;
  buildStatusGo = pkgs.callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper; };

  args = removeAttrs args' [ "config" "goBuildFlags" "goBuildLdFlags" ]; # Remove mobile-only arguments from args
  buildStatusGoMobileLib = buildStatusGo (args // {
    nativeBuildInputs = [ gomobile ] ++ lib.optional (targetConfig.name == "android") openjdk;

    buildMessage = "Building mobile library for ${targetConfig.name}";
    # Build mobile libraries
    # TODO: Manage to pass -s -w to -ldflags. Seems to only accept a single flag
    buildPhase = ''
      GOPATH=${gomobile.dev}:$GOPATH \
      PATH=${lib.makeBinPath [ gomobile.bin ]}:$PATH \
      ${lib.concatStringsSep " " targetConfig.envVars} \
      gomobile bind ${goBuildFlags} -target=${targetConfig.name} ${lib.concatStringsSep " " targetConfig.gomobileExtraFlags} \
                    -o ${targetConfig.outputFileName} \
                    ${goBuildLdFlags} \
                    ${goPackagePath}/mobile
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
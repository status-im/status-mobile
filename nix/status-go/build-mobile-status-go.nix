
{ buildGoPackage, go, gomobile, openjdk, xcodeWrapper, pkgs, stdenv }:

{ owner, repo, rev, version, goPackagePath, src, host,
  goBuildFlags, goBuildLdFlags,
  config } @ args':

with stdenv;

let
  args = removeAttrs args' [ "config" "goBuildFlags" "goBuildLdFlags" ];
  targetConfig = config;
  buildStatusGo = pkgs.callPackage ./build-status-go.nix { inherit buildGoPackage go xcodeWrapper; };

  buildStatusGoMobileLib = buildStatusGo (args // {
    nativeBuildInputs = [ gomobile ] ++ lib.optional (targetConfig.name == "android") openjdk;

    buildPhase = ''
      runHook preBuild

      runHook renameImports

      # Build mobile libraries
      # TODO: Manage to pass -s -w to -ldflags. Seems to only accept a single flag
      echo
      echo "Building mobile library for ${targetConfig.name}"
      echo
      GOPATH=${gomobile.dev}:$GOPATH \
      PATH=${lib.makeBinPath [ gomobile.bin ]}:$PATH \
      ${lib.concatStringsSep " " targetConfig.envVars} \
      gomobile bind ${goBuildFlags} -target=${targetConfig.name} ${lib.concatStringsSep " " targetConfig.gomobileExtraFlags} \
                    -o ${targetConfig.outputFileName} \
                    ${goBuildLdFlags} \
                    ${goPackagePath}/mobile

      runHook postBuild
    '';

    installPhase = ''
      runHook preInstall

      mkdir -p $out/lib
      mv ${targetConfig.outputFileName} $out/lib/

      runHook postInstall
    '';

    outputs = [ "out" ];

    meta = {
      platforms = with lib.platforms; linux ++ darwin;
    };
  });

in buildStatusGoMobileLib
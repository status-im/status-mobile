{ stdenv, utils, callPackage,
  buildGoPackage, go, gomobile, androidPkgs,
  openjdk, unzip, zip, xcodeWrapper }:

{ owner, repo, rev, cleanVersion, goPackagePath, src, host,

  # mobile-only arguments
  goBuildFlags, goBuildLdFlags,
  targetConfig } @ args':

let
  inherit (stdenv.lib) concatStringsSep makeBinPath optional optionals;

  buildStatusGo = callPackage ../build.nix {
    inherit buildGoPackage go xcodeWrapper utils;
  };

  # Remove mobile-only arguments from args
  args = removeAttrs args' [
    "targetConfig" "goBuildFlags" "goBuildLdFlags"
  ];

  buildStatusGoMobileLib =
    let
      inherit (stdenv.lib) concatStrings mapAttrsToList optionalString;
    in buildStatusGo (args // {
      nativeBuildInputs = [ gomobile unzip zip ] ++ optional (targetConfig.name == "android") openjdk;

      buildMessage = "Building mobile library for ${targetConfig.name}";
      # Build mobile libraries
      buildPhase =
        let
          NIX_GOWORKDIR = "$NIX_BUILD_TOP/go-build";
          CGO_LDFLAGS = concatStringsSep " " (goBuildLdFlags ++ [ "-extldflags=-Wl,--allow-multiple-definition" ]);
          nimbusBridgeVendorDir = "$NIX_BUILD_TOP/go/src/${goPackagePath}/vendor/${goPackagePath}/eth-node/bridge/nimbus";
        in ''
        mkdir ${NIX_GOWORKDIR}

        export GO111MODULE=off
        export GOPATH=${gomobile.dev}:$GOPATH
        export PATH=${makeBinPath [ gomobile.bin ]}:$PATH
        export NIX_GOWORKDIR=${NIX_GOWORKDIR}
        export ${concatStringsSep " " targetConfig.envVars}

        # Build the Go library using gomobile for each of the configured platforms
        ${concatStrings (mapAttrsToList (_: platformConfig: ''

          ${optionalString platformConfig.linkNimbus ''
          # Copy the Nimbus API artifacts to the expected vendor location
          cp ${platformConfig.nimbus}/{include/*,lib/libnimbus.a} ${nimbusBridgeVendorDir}
          chmod +w ${nimbusBridgeVendorDir}/libnimbus.{a,h}
          ''}

          echo
          echo "Building for target ${platformConfig.gomobileTarget}"
          gomobile bind \
            -target=${platformConfig.gomobileTarget} \
            -ldflags="${CGO_LDFLAGS}" \
            ${concatStringsSep " " targetConfig.gomobileExtraFlags} \
            ${goBuildFlags} \
            -o ${platformConfig.outputFileName} \
            ${goPackagePath}/mobile

          ${optionalString platformConfig.linkNimbus ''
          rm ${nimbusBridgeVendorDir}/libnimbus.{a,h}
          ''}
          '') targetConfig.platforms)
        }

        if [ "${targetConfig.name}" = 'android' ]; then
          # Merge the platform-specific .aar files into a single one
          local mergeDir='.aar'
          mkdir $mergeDir
          ${concatStrings (mapAttrsToList (_: platformConfig: ''
            unzip -d $mergeDir -q -n -u ${platformConfig.outputFileName}
            rm ${platformConfig.outputFileName}
          '') targetConfig.platforms)}
          pushd $mergeDir > /dev/null
            zip -r -o ../${targetConfig.outputFileName} *
          popd > /dev/null
          rm -rf $mergeDir
          unzip -l ${targetConfig.outputFileName}
        fi

        # TODO: Merge iOS packages when linking with libnimbus.a

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

{ stdenv, buildGoPackage, go, pkgs, fetchFromGitHub, openjdk, androidPkgs, composeXcodeWrapper, xcodewrapperArgs ? {} }:

with stdenv;

let
  gomobile = pkgs.callPackage ./gomobile { inherit (androidPkgs) platform-tools; inherit composeXcodeWrapper xcodewrapperArgs; };
  version = lib.fileContents ../../STATUS_GO_VERSION; # TODO: Simplify this path search with lib.locateDominatingFile
  owner = lib.fileContents ../../STATUS_GO_OWNER;
  repo = "status-go";
  goPackagePath = "github.com/${owner}/${repo}";
  rev = version;
  sha256 = lib.fileContents ../../STATUS_GO_SHA256;
  mobileConfigs = {
    android = {
      name = "android";
      outputFileName = "status-go-${version}.aar";
      envVars = ''
        ANDROID_HOME=${androidPkgs.androidsdk}/libexec/android-sdk \
        ANDROID_NDK_HOME="${androidPkgs.ndk-bundle}/libexec/android-sdk/ndk-bundle" \
      '';
      gomobileExtraFlags = "";
    };
    ios = {
      name = "ios";
      outputFileName = "Statusgo.framework";
      envVars = "";
      gomobileExtraFlags = "-iosversion=8.0";
    };
  };
  hostConfigs = {
    darwin = {
      mobileTargets = [ mobileConfigs.android mobileConfigs.ios ];
      desktopOutputFileName = "libstatus.a";
    };
    linux = {
      mobileTargets = [ mobileConfigs.android ];
      desktopOutputFileName = "libstatus.a";
    };
  };
  currentHostConfig = if isDarwin then hostConfigs.darwin else hostConfigs.linux;
  currentHostMobileTargets = currentHostConfig.mobileTargets;
  mobileBuildScript = lib.concatMapStrings (target: ''
    echo
    echo "Building mobile library for ${target.name}"
    echo
    GOPATH=${gomobile.dev}:$GOPATH \
    PATH=${lib.makeBinPath [ gomobile.bin openjdk ]}:$PATH \
    ${target.envVars} \
    gomobile bind ${goBuildFlags} -target=${target.name} ${target.gomobileExtraFlags} \
      -o ${target.outputFileName} \
      ${goBuildLdFlags} \
      ${goPackagePath}/mobile
  '') currentHostMobileTargets;
  mobileInstallScript = lib.concatMapStrings (target: ''
    mkdir -p $out/lib/${target.name}
    mv ${target.outputFileName} $out/lib/${target.name}/
  '') currentHostMobileTargets;
  desktopOutputFileName = currentHostConfig.desktopOutputFileName;
  desktopSystem = hostPlatform.system;
  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${lib.concatMapStrings (ref: " -t ${ref}") refs}'';
  goBuildFlags = "-v";
  goBuildLdFlags = "-ldflags=-s";
  xcodeWrapper = composeXcodeWrapper xcodewrapperArgs;

in buildGoPackage rec {
  inherit goPackagePath version rev;
  name = "${repo}-${version}";

  src = pkgs.fetchFromGitHub { inherit rev owner repo sha256; };

  nativeBuildInputs = [ gomobile openjdk ]
    ++ lib.optional isDarwin xcodeWrapper;

  # Fixes Cgo related build failures (see https://github.com/NixOS/nixpkgs/issues/25959 )
  hardeningDisable = [ "fortify" ];

  # gomobile doesn't seem to be able to pass -ldflags with multiple values correctly to go build, so we just patch files here  
  patchPhase = ''
    date=$(date -u '+%Y-%m-%d.%H:%M:%S')

    substituteInPlace cmd/statusd/main.go --replace \
      "buildStamp = \"N/A\"" \
      "buildStamp = \"$date\""
    substituteInPlace params/version.go --replace \
      "var Version string" \
      "var Version string = \"${version}\""
    substituteInPlace params/version.go --replace \
      "var GitCommit string" \
      "var GitCommit string = \"${rev}\""
    substituteInPlace vendor/github.com/ethereum/go-ethereum/metrics/metrics.go --replace \
      "var EnabledStr = \"false\"" \
      "var EnabledStr = \"true\""
  '';

  # we print out the version so that we fail fast in case there's any problem running xcrun, instead of failing at the end of the build
  preConfigure = lib.optionalString isDarwin ''
    xcrun xcodebuild -version
  '';

  buildPhase = ''
    runHook preBuild

    runHook renameImports

    pushd "$NIX_BUILD_TOP/go/src/${goPackagePath}" >/dev/null

    echo
    echo "Building desktop library"
    echo
    #GOOS=windows GOARCH=amd64 CGO_ENABLED=1 go build ${goBuildFlags} -buildmode=c-archive -o $out/${desktopOutputFileName} ./lib
    go build -o $out/${desktopOutputFileName} ${goBuildFlags} -buildmode=c-archive ${goBuildLdFlags} ./lib

    # Build command-line tools
    for name in ./cmd/*; do
      echo
      echo "Building $name"
      echo
  	  go install ${goBuildFlags} $name
    done

    popd >/dev/null

    # Build mobile libraries
    # TODO: Manage to pass -s -w to -ldflags. Seems to only accept a single flag
    ${mobileBuildScript}

    runHook postBuild
  '';

  postInstall = ''
    mkdir -p $bin
    cp -r "$NIX_BUILD_TOP/go/bin/" $bin
   
    ${mobileInstallScript}

    mkdir -p $out/lib/${desktopSystem} $out/include
    mv $out/${desktopOutputFileName} $out/lib/${desktopSystem}
    mv $out/libstatus.h $out/include
  '';

  # remove hardcoded paths to go package in /nix/store, otherwise Nix will fail the build
  preFixup = ''
    find $out -type f -exec ${removeExpr removeReferences} '{}' + || true
  '';

  outputs = [ "out" "bin" ];

  meta = {
    description = "The Status module that consumes go-ethereum.";
    license = lib.licenses.mpl20;
    maintainers = with lib.maintainers; [ pombeirp ];
    platforms = with lib.platforms; linux ++ darwin;
  };
}

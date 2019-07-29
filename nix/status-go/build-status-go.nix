{ buildGoPackage, go, xcodeWrapper, stdenv, utils }:

{ owner, repo, rev, version, goPackagePath, src, host,
  nativeBuildInputs ? [],
  buildPhase, buildMessage,
  installPhase ? "",
  postInstall ? "",
  preFixup ? "",
  outputs, meta } @ args':

with stdenv;

let
  inherit (stdenv.lib) strings;

  removeReferences = [ go ];
  removeExpr = refs: ''remove-references-to ${lib.concatMapStrings (ref: " -t ${ref}") refs}'';

  args = removeAttrs args' [ "buildMessage" ]; # Remove our arguments from args before passing them on to buildGoPackage
  buildStatusGo = buildGoPackage (args // {
    pname = repo;
    version = "${version}-${strings.substring 0 7 rev}-${host}";

    nativeBuildInputs = 
      nativeBuildInputs ++
      lib.optional isDarwin xcodeWrapper;

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

    # Ensure XCode is present, instead of failing at the end of the build
    preConfigure = lib.optionalString isDarwin utils.enforceXCodeAvailable;

    buildPhase = ''
      runHook preBuild

      runHook renameImports

      echo
      echo "${buildMessage}"
      echo
      ${buildPhase}

      runHook postBuild
    '';

    installPhase = ''
      runHook preInstall

      ${installPhase}

      runHook postInstall
    '';

    # replace hardcoded paths to go package in /nix/store, otherwise Nix will fail the build
    preFixup = ''
      ${preFixup}

      find $out -type f -exec ${removeExpr removeReferences} '{}' + || true
      return
    '';

    passthru = { inherit owner version rev; };

    meta = {
      # Add default meta information
      inherit (meta) platforms;
      description = meta.description or "The Status module that consumes go-ethereum.";
      license = lib.licenses.mpl20;
    } // meta // {
      # add an extra maintainer to every package
      maintainers = (meta.maintainers or [ ]) ++ [ lib.maintainers.pombeirp ];
    };
  });

in buildStatusGo

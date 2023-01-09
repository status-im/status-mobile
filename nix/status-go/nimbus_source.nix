{ utils, lib, fetchFromGitHub }:

let
  inherit (lib) strings traceValFn importJSON getConfig;

  srcOverride = getConfig "nimbus-eth1.src-override" null;
  # Warning message about using local sources
  localSrcWarn = (path: "Using local nimbus-eth1 sources from ${path}");

  localSrc = rec {
    owner = "status-im";
    repo = "nimbus-eth1";
    rev = "unknown";
    shortRev = rev;
    rawVersion = "develop";
    cleanVersion = rawVersion;
    goPackagePath = "github.com/${owner}/${repo}";
    # We use builtins.path so that we can name the resulting derivation,
    # Normally the name would not be deterministic, taken from the checkout directory.
    src = builtins.path rec {
      path = traceValFn localSrcWarn srcOverride;
      name = "${repo}-source-${shortRev}";
      # Keep this filter as restrictive as possible in order
      # to avoid unnecessary rebuilds and limit closure size
      filter = lib.mkFilter {
        root = path;
        include = [ ".*" ];
        exclude = [
          ".*/[.]git.*" ".*[.]md" ".*[.]yml" ".*/.*_test.go$"
          "VERSION" "_assets/.*" "build/.*"
          ".*/.*LICENSE.*" ".*/CONTRIB.*" ".*/AUTHOR.*"
        ];
      };
    };
  };

  githubSrc = let
    owner = "status-im";
    repo = "nimbus-eth1";
    rev = "1199e476397e271b889e54680dc632e47bc01dd6";
    sha256 = "sha256-egx+I4h/V/56QRBtlaZffc3hrq7IaZCqL2Lm5XNqH1c=";
    version = "0.1";
  in rec {
    inherit owner repo rev sha256 version;
    shortRev = strings.substring 0 7 rev;
    rawVersion = version;
    cleanVersion = utils.sanitizeVersion version;
    src = fetchFromGitHub {
      inherit rev owner repo sha256;
      name = "${repo}-${shortRev}-source-with-submodules";
      fetchSubmodules = true;
    };
  };
in
  if srcOverride != null
  # If config.status-im.status-go.src-override is defined,
  # instruct Nix to use that path to build status-go
  then localSrc
  # Otherwise grab it from the location defined by status-go-version.json
  else githubSrc

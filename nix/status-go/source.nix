{ lib, fetchFromGitHub }:

let
  inherit (lib) strings traceValFn importJSON getEnvWithDefault;

  # TODO: move to default?
  srcOverride = getEnvWithDefault "STATUS_GO_SRC_OVERRIDE" null;
  # Warning message about using local sources
  localSrcWarn = (path: "Using local status-go sources from ${path}");

  localSrc = rec {
    owner = "status-im";
    repo = "status-go";
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
    # TODO: Simplify this path search with lib.locateDominatingFile
    versionJSON = importJSON ../../status-go-version.json;
    sha256 = versionJSON.src-sha256;
  in rec {
    inherit (versionJSON) owner repo version;
    rev = versionJSON.commit-sha1;
    shortRev = strings.substring 0 7 rev;
    rawVersion = versionJSON.version;
    cleanVersion = lib.sanitizeVersion versionJSON.version;
    # Need to pretend this is from status-im to let Go build it.
    goPackagePath = "github.com/status-im/${repo}";
    src = fetchFromGitHub {
      inherit rev owner repo sha256;
      name = "${repo}-${shortRev}-source";
    };
  };
in
  if srcOverride != null
  # If config.status-im.status-go.src-override is defined,
  # instruct Nix to use that path to build status-go
  then localSrc
  # Otherwise grab it from the location defined by status-go-version.json
  else githubSrc

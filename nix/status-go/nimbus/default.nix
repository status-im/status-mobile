#
# This fetches nimbus source from local folder or GitHub and
# loads the derivation defined in the `nix/default.nix` in the repo.
#

{ config, lib, stdenv, callPackage, fetchFromGitHub }:

let
  inherit (lib) getConfig attrByPath strings traceValFn;

  repo = "nimbus";

  localPath = getConfig "nimbus.src-override" "";
  localSrc =
    # We use builtins.path so that we can name the resulting derivation.
    # Name would be taken from the checkout directory, and wouldn't be deterministic.
    builtins.path rec {
      path = traceValFn (path: ''
        Using local ${repo} sources from ${path}
      '') localPath;
      name = "${repo}-source-local";
      filter =
        # Keep this filter as restrictive as possible in order to
        # avoid unnecessary rebuilds and limit closure size
        lib.mkFilter {
          root = path;
          include = [
            "nix.*" "wrappers.*" "vendor.*"
            "Makefile" "nim.cfg" "nimbus.nimble" "default.nix"
          ];
        };
    };

  ghSrc = fetchFromGitHub rec {
    inherit repo;
    name = "${repo}-source-${strings.substring 0 7 rev}";
    rev = "73278f20d0bf27fb7c6c331b515abb765814f1cc";
    owner = "status-im";
    sha256 = "0myq234zqnpmqsc2452xygnyc6sjs8x1blyrpa4bi9v2cwbyap5c";
    fetchSubmodules = true;
  };

  src = if localPath != "" then localSrc else ghSrc;

  nimbusDeriv = import "${src}/nix/default.nix";

in nimbusDeriv

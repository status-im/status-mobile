{ config, stdenv, callPackage, fetchFromGitHub, mkFilter }:

let
  inherit (stdenv.lib) attrByPath strings traceValFn;

  repo = "nimbus";

  localPath = attrByPath ["status-im" "nimbus" "src-override"] "" config;
  localSrc = builtins.path rec { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
    path = traceValFn (path: "Using local ${repo} sources from ${path}\n") localPath;
    name = "${repo}-source-local";
    filter =
      # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
      mkFilter {
        root = path;
        include = [
          "nix.*" "wrappers.*" "vendor.*"
          "Makefile" "nim.cfg" "nimbus.nimble" "default.nix"
        ];
      };
  };

  src = if localPath != "" then localSrc
        else fetchFromGitHub rec {
          inherit repo;
          name = "${repo}-source-${strings.substring 0 7 rev}";
          rev = "73278f20d0bf27fb7c6c331b515abb765814f1cc";
          owner = "status-im";
          sha256 = "0myq234zqnpmqsc2452xygnyc6sjs8x1blyrpa4bi9v2cwbyap5c";
          fetchSubmodules = true;
        };
  nimbusDeriv = import "${src}/nix/default.nix";

in nimbusDeriv

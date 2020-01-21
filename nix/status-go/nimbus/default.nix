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
        dirRootsToInclude = [ "nix" "wrappers" "vendor" ];
        dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".vscode" ".dependabot" ".github" "examples" "docs" ];
        filesToInclude = [ "Makefile" "nim.cfg" "nimbus.nimble" "default.nix" ];
        root = path;
      };
  };

  src = if localPath != "" then localSrc
        else fetchFromGitHub rec {
          inherit repo;
          name = "${repo}-source-${strings.substring 0 7 rev}";
          rev = "501455b0cd2e74c451bc1743e2f1070a3fee1343";
          owner = "status-im";
          sha256 = "0nxh3hh8fib3hlmvs5d67h6cq3kyap94pa9w7ixsfa5285ila17h";
          fetchSubmodules = true;
        };
  nimbusDeriv = import "${src}/nix/default.nix";

in nimbusDeriv

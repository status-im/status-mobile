#
# This Nix expression builds an index.*.js file for the current repository given a node modules Nix expression
#

{ stdenv, stdenvNoCC, lib, target-os, callPackage, pkgs,
  mkFilter, clojure, leiningen, maven, nodejs, localMavenRepoBuilder }:

# The Nix expression takes a second argument to specify the node dependencies
{ projectNodePackage }:

let
  lein-command = if target-os == "all" then "lein prod-build" else "lein prod-build-${target-os}";
  lein-project-deps = import ../lein/lein-project-deps.nix { };
  leinProjectDepsLocalRepo = localMavenRepoBuilder "lein-project-deps" lein-project-deps;

in stdenv.mkDerivation {
  name = "prod-build-${target-os}";
  src =
    let path = ./../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-prod-build-source";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        mkFilter {
          dirRootsToInclude = [ 
            "components/src" "react-native/src/cljsjs" "react-native/src/mobile" "src" "env/prod" "prod" # Taken from project.clj :profiles :prod :cljsbuild :builds :android :source-paths
            "resources" "status-modules/cljs" "status-modules/resources"
          ];
          dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ".gradle" "build" "intermediates" "libs" "obj" ];
          filesToInclude = [ "build.clj" "externs.js" "project.clj" "prepare-modules.js" ];
          root = path;
        };
    };
  buildInputs = [ clojure leiningen nodejs ];

  LEIN_OFFLINE = "y";

  phases = [ "unpackPhase" "patchPhase" "buildPhase" "installPhase" ];
  patchPhase =
    let anchor = '':url "https://github.com/status-im/status-react/"'';
    in ''
      substituteInPlace project.clj \
        --replace '${anchor}' \
                  '${anchor}
        :local-repo "${leinProjectDepsLocalRepo}"' \
        --replace '[rasom/lein-githooks "' ';; [rasom/lein-githooks "' \
        --replace ':githooks' ';; :githooks' \
        --replace ':pre-commit' ';; :pre-commit'
    '';
  buildPhase = ''
    ln -s ${projectNodePackage}/node_modules

    # On macOS, lein tries to create $HOME/.lein, which fails with java.lang.Exception: Couldn't create directories: /homeless-shelter/.lein, so we just make it use a temp dir
    tmp=$(mktemp -d)
    HOME=$tmp ${lein-command}
    rm -rf $tmp
    unset tmp

    node prepare-modules.js
  '';
  installPhase = ''
    mkdir -p $out
    cp index.*.js $out/
  '';
}

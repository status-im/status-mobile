#
# This Nix expression builds an index.*.js file for the current repository given a node modules Nix expression
#

{ target-os ? "android",
  stdenv, lib, clojure, leiningen, nodejs, bash, git,
  leinProjectDeps, localMavenRepoBuilder, projectNodePackage }:

let
  lein-command = if target-os == "all" then "lein jsbundle" else "lein jsbundle-${target-os}";
  leinProjectDepsLocalRepo = localMavenRepoBuilder "lein-project-deps" leinProjectDeps;

in stdenv.mkDerivation {
  name = "status-react-build-jsbundle-${target-os}";
  src =
    let path = ./../../../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-source-jsbundle";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        lib.mkFilter {
          root = path;
          ignoreVCS = false;
          include = [ 
            "VERSION" "BUILD_NUMBER" "scripts/version/.*"
            # I want to avoid including the whole .git directory
            ".git/HEAD" ".git/objects" ".git/refs/heads/.*"
            "src/.*" "prod/.*" "env/prod/.*"
            "components/src/.*" 
            "react-native/src" 
            "react-native/src/cljsjs/.*"
            "react-native/src/mobile/.*"
            "status-modules/cljs/.*"
            "status-modules/resources/.*"
            "build.clj" "externs.js"
            "project.clj" "prepare-modules.js"
            # lein jsbundle stat's images to check if they exist
            "resources/.*"
          ];
          exclude = [
            "resources/fonts/.*"
          ];
        };
    };
  buildInputs = [ clojure leiningen nodejs bash git ];

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

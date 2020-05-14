#
# This Nix expression builds the js files for the current repository given a node modules Nix expression
#

{ stdenv, lib, deps, pkgs }:

stdenv.mkDerivation {
  name = "status-react-build-jsbundle-android";
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
            "src/.*" "shadow-cljs.edn" "index.js"
            # shadow-cljs expects these for deps resolution
            "mobile/js_files/package.json" "mobile/js_files/yarn.lock"
            # build stat's images to check if they exist
            "resources/.*" "translations/.*"
          ];
          exclude = [
            "resources/fonts/.*"
          ];
        };
    };
  buildInputs = with pkgs; [ clojure nodejs bash git openjdk];

  phases = [ "unpackPhase" "patchPhase" "configurePhase" "buildPhase" "installPhase" ];
  # Patching shadow-cljs.edn so it uses the local maven repo of dependencies provided by Nix
  patchPhase =
    let anchor = ''{:source-paths ["src" "test/cljs"]'';
    in ''
      substituteInPlace shadow-cljs.edn \
        --replace '${anchor}' \
                  '${anchor}
       :maven {:local-repo "${deps.clojure}"}'
    '';
  configurePhase = ''
    # Symlink Node.js modules to build directory
    ln -s ${deps.nodejs}/node_modules

    # Symlink Node.JS dependency definitions
    ln -sf mobile/js_files/package.json ./
    ln -sf mobile/js_files/yarn.lock ./
  '';
  buildPhase = ''
    # Assemble CLASSPATH from available clojure dependencies.
    # We append 'src' so it can find the local sources.
    export CLASS_PATH="$(find ${deps.clojure} \
      -iname '*.jar' | tr '\n' ':')src"

    # target must be one of the builds defined in shadow-cljs.edn
    java -cp "$CLASS_PATH" clojure.main \
      -m shadow.cljs.devtools.cli release mobile
  '';
  installPhase = ''
    mkdir -p $out
    cp -r index.js app $out/
  '';
}

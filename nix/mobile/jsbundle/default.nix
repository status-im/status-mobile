#
# This Nix expression builds the js files for the current repository given a node modules Nix expression
#

{ stdenv, lib, deps, pkgs }:

# Path to the file containing secret environment variables
{ secretsFile ? "" }:

stdenv.mkDerivation {
  name = "status-mobile-jsbundle";
  src =
    let path = ./../../..;
    # We use builtins.path so that we can name the resulting derivation,
    # otherwise the name would be taken from the checkout directory,
    # which is outside of our control inherit path;
    in builtins.path {
      inherit path;
      name = "status-mobile-source-jsbundle";
      filter =
        # Keep this filter as restrictive as possible in order to avoid
        # unnecessary rebuilds and limit closure size
        lib.mkFilter {
          root = path;
          ignoreVCS = false;
          include = [
            "VERSION" "BUILD_NUMBER" "scripts/version/.*"
            "src/.*" "shadow-cljs.edn"
            # I want to avoid including the whole .git directory
            ".git/HEAD" ".git/objects" ".git/refs/heads/.*"
            # shadow-cljs expects these for deps resolution
            "package.json" "yarn.lock"
            # build stat's images to check if they exist
            "resources/.*" "translations/.*"
          ];
          exclude = [
            "resources/fonts/.*"
          ];
        };
    };
  buildInputs = with pkgs; [ clojure nodejs bash git openjdk];
  phases = [
    "unpackPhase" "secretsPhase" "patchPhase"
    "configurePhase" "buildPhase" "installPhase"
  ];

  # For optional POKT_TOKEN variable
  secretsPhase = if (secretsFile != "") then ''
    source "${secretsFile}"
    ${lib.checkEnvVarSet "POKT_TOKEN"}
  '' else ''
    echo "No secrets provided!"
  '';

  # Patching shadow-cljs.edn so it uses the local maven repo of dependencies provided by Nix
  patchPhase =
    let anchor = ''{:source-paths ["src" "test/cljs"]'';
    in ''
      substituteInPlace shadow-cljs.edn \
        --replace-quiet '${anchor}' \
                  '${anchor}
       :maven {:local-repo "${deps.clojure}"}'
    '';
  configurePhase = ''
    # Symlink Node.js modules to build directory
    ln -s ${deps.nodejs}/node_modules
  '';
  buildPhase = ''
    # Assemble CLASSPATH from available clojure dependencies.
    # We prepend 'src' so it can find the local sources and prioritize
    # our own namespaces over dependencies, given that indirect dependencies
    # can also cause naming conflicts (e.g. prismatic/schema already uses
    # namespace schema.core).
    export CLASS_PATH="src:$(find ${deps.clojure} \
      -iname '*.jar' | tr '\n' ':')"

    # target must be one of the builds defined in shadow-cljs.edn
    java -cp "$CLASS_PATH" clojure.main \
      -m shadow.cljs.devtools.cli release mobile
  '';
  installPhase = ''
    mkdir -p $out
    cp -r result/* $out/
  '';
}

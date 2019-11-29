{ callPackage, lib, stdenv, mkShell, mergeSh, mkFilter, 
  xcodeWrapper, projectNodePackage, status-go,
  flock, procps, watchman, bundler, fastlane }:

let
  inherit (lib) catAttrs unique;

  pod = callPackage ./pod-shell.nix { };
  status-go-shell = callPackage ./status-go-shell.nix { inherit status-go; };

  selectedSources = [ status-go fastlane ];

  src =
    let path = ./../../..;
    in builtins.path { # We use builtins.path so that we can name the resulting derivation, otherwise the name would be taken from the checkout directory, which is outside of our control
      inherit path;
      name = "status-react-source-npm-deps";
      filter =
        # Keep this filter as restrictive as possible in order to avoid unnecessary rebuilds and limit closure size
        mkFilter {
          dirRootsToInclude = [ "mobile/js_files" ];
          dirsToExclude = [ ".git" ".svn" "CVS" ".hg" ];
          filesToInclude = [ ".babelrc" ];
          root = path;
        };
    };

  buildInputs = unique ([
    xcodeWrapper watchman bundler procps
    flock # used in reset-node_modules.sh
  ] ++ catAttrs "buildInputs" selectedSources);

  shellHook = ''
    pushd "$STATUS_REACT_HOME" > /dev/null
    {
      # Set up symlinks to mobile enviroment in project root 
      ln -sf ./mobile/js_files/metro.config.js ./metro.config.js
      ln -sf ./mobile/js_files/package.json ./package.json
      ln -sf ./mobile/js_files/yarn.lock ./yarn.lock

      # check if node modules changed and if so install them
      ./nix/mobile/reset-node_modules.sh "${projectNodePackage}"
    }
    popd > /dev/null
  '';

  localShell = mkShell {
    inherit buildInputs shellHook;
  };

in {
  inherit shellHook buildInputs pod;

  shell = mergeSh localShell [
    fastlane.shell status-go-shell pod.shell
  ];
}

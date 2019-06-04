#
# This Nix expression appends/modifies an existing attribute set in order to run scripts/setup if needed, 
#  as well as define STATUS_REACT_HOME
#

{ stdenv, mkShell, git }:

let
  shell' = shellAttr:
    shellAttr // {
      nativeBuildInputs = (shellAttr.nativeBuildInputs or []) ++ [ git ];
      shellHook = ''
        set -e

        export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)

        ${shellAttr.shellHook or ""}

        if [ "$IN_NIX_SHELL" != 'pure' ] && [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
          $STATUS_REACT_HOME/scripts/setup
          touch $STATUS_REACT_HOME/.ran-setup
        fi

        set +e
      '';
    };
  # Declare a specialized mkShell function which adds some bootstrapping
  #  so that e.g. STATUS_REACT_HOME is automatically available in the shell
  mkShell' = shellAttr: (mkShell.override { inherit stdenv; }) (shell' shellAttr);

in mkShell'

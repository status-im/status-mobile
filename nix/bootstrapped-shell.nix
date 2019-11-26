#
# This Nix expression appends/modifies an existing attribute set in order to run scripts/setup if needed, 
#  as well as define STATUS_REACT_HOME
#

{ stdenv, mkShell, target-os, git }:

# Declare a specialized mkShell function which adds some bootstrapping
#  so that e.g. STATUS_REACT_HOME is automatically available in the shell
attrs:
  (mkShell.override({ inherit stdenv; }) attrs)
          .overrideAttrs(super: {
    nativeBuildInputs = (super.nativeBuildInputs or [ ]) ++ [ git ];
    TARGET_OS = target-os;
    shellHook = ''
      set -e

      export LANG="en_US.UTF-8"
      export LANGUAGE="en_US.UTF-8"

      export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)

      ${super.shellHook or ""}

      if [ "$IN_NIX_SHELL" != 'pure' ] && [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
        $STATUS_REACT_HOME/scripts/setup
        touch $STATUS_REACT_HOME/.ran-setup
      fi

      set +e
    '';
  })

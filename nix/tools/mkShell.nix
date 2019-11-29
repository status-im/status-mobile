# This Nix expression appends/modifies an existing attribute set
# in order to define STATUS_REACT_HOME for use multiple derivations and scripts

{ pkgs, stdenv ? pkgs.stdenv }:

# Declare a specialized mkShell function which adds some bootstrapping
#  so that e.g. STATUS_REACT_HOME is automatically available in the shell
attrs:
  (pkgs.mkShell.override({ inherit stdenv; }) attrs)
    .overrideAttrs(super: rec {
      nativeBuildInputs = (super.nativeBuildInputs or [ ]) ++ [ pkgs.git ];

      # avoid terinal issues
      TERM="xterm";

      # default locale
      LANG="en_US.UTF-8";
      LANGUAGE="en_US.UTF-8";

      shellSetup = ''
        set -e

        if [ -z "$STATUS_REACT_HOME" ]; then
          export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)
        fi

        export SHELL_SETUP=done

        set +e
      '';

      shellHook = ''
        if [ -z "$SHELL_SETUP" ]; then
          ${shellSetup}
        fi

        ${super.shellHook or ""}
      '';
    }
  )

{ git }:

shell:

shell // {
  nativeBuildInputs = (shell.nativeBuildInputs or []) ++ [ git ];
  shellHook = ''
    set -e

    export STATUS_REACT_HOME=$(git rev-parse --show-toplevel)

    ${shell.shellHook or ""}

    if [ "$IN_NIX_SHELL" != 'pure' ] && [ ! -f $STATUS_REACT_HOME/.ran-setup ]; then
      $STATUS_REACT_HOME/scripts/setup
      touch $STATUS_REACT_HOME/.ran-setup
    fi
    set +e
  '';
}

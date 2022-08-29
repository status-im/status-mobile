{ mkShell, deps }:

mkShell {
  # check if node modules changed and if so install them
  shellHook = ''
    export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
    "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${deps.nodejs-patched}
  '';
}

{ mkShell, deps }:

mkShell {
  # Fix for ERR_OSSL_EVP_UNSUPPORTED error.
  NODE_OPTIONS = "--openssl-legacy-provider";

  # check if node modules changed and if so install them
  shellHook = ''
    export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
    "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${deps.nodejs-patched}
  '';
}

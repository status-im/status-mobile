{ mkShell, nodejs, deps }:

mkShell {
  # Check if node modules changed and if so install them.
  shellHook = ''
    # Fix for ERR_OSSL_EVP_UNSUPPORTED error.
    export NODE_OPTIONS="--openssl-legacy-provider";

    # Fix Xcode using system Node.js version.
    export NODE_BINARY="${nodejs}/bin/node";

    export STATUS_MOBILE_HOME=$(git rev-parse --show-toplevel)
    "$STATUS_MOBILE_HOME/nix/scripts/node_modules.sh" ${deps.nodejs-patched}
  '';
}

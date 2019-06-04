{ pkgs, nodejs }:

#
# This expression builds the realm executable that is used for debugging the desktop app (used by ubuntu-server.js)
#

let nodePackages = import ./output { inherit pkgs nodejs; };
    realm-version = "5.12.1";
    realm-patched-name = "realm-https://github.com/status-im/realm-js/archive/v2.20.1.tar.gz";
    # We download ${realm-core-src} to ${realm-dest-dir} in order to avoid having realm try to download these files on its own (which is disallowed by Nix)
    realm-core-src = pkgs.fetchurl (
      if builtins.currentSystem == "x86_64-darwin" then {
        url = "https://static.realm.io/downloads/core/realm-core-Release-v${realm-version}-Darwin-devel.tar.gz";
        sha256 = "05ji1zyskwjj8p5i01kcg7h1cxdjj62fcsp6haf2f65qshp6r44d";
      } else {
        url = "https://static.realm.io/downloads/core/realm-core-Release-v${realm-version}-Linux-devel.tar.gz";
        sha256 = "02pvi28qnvzdv7ghqzf79bxn8id9s7mpp3g2ambxg8jrcrkqfvr1";
      }
    );
    realm-dest-dir = if builtins.currentSystem == "x86_64-darwin" then
      "$out/lib/node_modules/realm/compiled/node-v64_darwin_x64/realm.node" else
      "$out/lib/node_modules/realm/compiled/node-v64_linux_x64/realm.node";
in nodePackages // {
  "${realm-patched-name}" = nodePackages."${realm-patched-name}".override(oldAttrs: {
    reconstructLock = true;
    preRebuild = ''
      # Do not attempt to do any http calls!
      substituteInPlace $out/lib/node_modules/realm/scripts/download-realm.js \
        --replace "!shouldSkipAcquire(realmDir, requirements, options.force)" "false"
      mkdir -p ${realm-dest-dir}
      tar -xzf ${realm-core-src} -C ${realm-dest-dir}
    '';
  });
}

{ pkgs, nodejs }:

let nodePackages = import ./output { inherit pkgs; inherit nodejs; };
    realm-version = "5.12.1";
    realm-patched-name = "realm-git+https://github.com/status-im/realm-js.git#heads/v2.20.1";
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
      "$out/lib/node_modules/realm/vendor/realm-darwin-x64" else
      "$out/lib/node_modules/realm/vendor/realm-linux-x64";
in nodePackages // {
  ${realm-patched-name} = nodePackages.${realm-patched-name}.override(oldAttrs: {
    buildInputs = oldAttrs.buildInputs ++ [ pkgs.nodePackages.node-pre-gyp ];
    reconstructLock = true;
    preRebuild = ''
      # Do not attempt to do any http calls!
      substituteInPlace $out/lib/node_modules/realm/scripts/download-realm.js \
        --replace "return acquire(requirements, realmDir)" ""
      mkdir -p ${realm-dest-dir}
      tar -xzf ${realm-core-src} -C ${realm-dest-dir}
    '';
  });
}

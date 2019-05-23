{ target-os, stdenv, lib, callPackage, fetchurl, pkgs, nodejs }:

let
  target-os' = if target-os == "windows" then "win" else 
               if target-os == "macos" then "darwin" else target-os;
  realm-version = "5.12.1";
  realm-patched-name = "realm-https://github.com/status-im/realm-js/archive/v2.20.1.tar.gz";
  realm-core-src = pkgs.fetchurl (
    if builtins.currentSystem == "x86_64-darwin" then {
      url = "https://static.realm.io/downloads/core/realm-core-Release-v${realm-version}-Darwin-devel.tar.gz";
      sha256 = "05ji1zyskwjj8p5i01kcg7h1cxdjj62fcsp6haf2f65qshp6r44d";
    } else {
      url = "https://static.realm.io/downloads/core/realm-core-Release-v${realm-version}-Linux-devel.tar.gz";
      sha256 = "02pvi28qnvzdv7ghqzf79bxn8id9s7mpp3g2ambxg8jrcrkqfvr1";
    }
  );
  realm-dest-dir = "$out/lib/node_modules/realm/compiled/node-v64_${target-os'}_x64/realm.node";
  nodePackages = callPackage ./node2nix-output {
    inherit pkgs nodejs;
  };
  nodepkgs = nodePackages // {
    ${realm-patched-name} = nodePackages.${realm-patched-name}.override(oldAttrs: {
      buildInputs = oldAttrs.buildInputs ++ [ pkgs.nodePackages.node-pre-gyp ];
      reconstructLock = true;
      preRebuild = ''
        # Do not attempt to do any http calls!
        substituteInPlace $out/lib/node_modules/realm/scripts/download-realm.js \
          --replace "!shouldSkipAcquire(realmDir, requirements, options.force)" "false"
        mkdir -p ${realm-dest-dir}
        tar -xzf ${realm-core-src} -C ${realm-dest-dir}
      '';
    });
  };
  pkg-tag = "v2.6";
  pkg-node-version = "10.15.3";

in stdenv.mkDerivation rec {
  name = "ubuntu-server-${target-os'}";
  src = ./.;

  pkg-fetch = fetchurl (if target-os' == "darwin" then {
    url = "https://github.com/zeit/pkg-fetch/releases/download/${pkg-tag}/uploaded-${pkg-tag}-node-v${pkg-node-version}-macos-x64";
    sha256 = "1bigx8n24brkdigg6dk3z3fpi9z70l3l12vfhdcqv86v05pxs7l5";
  } else {
    url = "https://github.com/zeit/pkg-fetch/releases/download/${pkg-tag}/uploaded-${pkg-tag}-node-v${pkg-node-version}-linux-x64";
    sha256 = "1f4ggpi6g49x3l0bljvvkazaq4b3f0jfjgpypz8arixqh5lanwns";
  });

  #buildInputs = (builtins.attrValues nodepkgs);
  buildInputs = [ nodepkgs."pkg-4.4.0" nodepkgs."${realm-patched-name}" ];# ++ (builtins.attrValues nodepkgs);
  # patchPhase = ''
  #   substituteInPlace node_modules/realm/lib/index.js \
  #     --replace "nodeRequire('./submit-analytics')" "require('./submit-analytics')" \
  #     --replace "nodeRequire('node-pre-gyp')" "require('node-pre-gyp')" \
  #     --replace "nodeRequire('path')" "require('path')"
  # '';

  buildPhase = ''
    build_dir="$(mktemp -d)"
    mkdir -p $build_dir/node_modules
    echo "${nodepkgs."react-native-git+https://github.com/status-im/react-native-desktop.git#v0.57.8_8"} -> $build_dir/node_modules"
    cp ./ubuntu-server.js $build_dir
    cp -R ${nodepkgs."${realm-patched-name}"}/lib/node_modules/ $build_dir
    ls -al ${nodepkgs."react-native-git+https://github.com/status-im/react-native-desktop.git#v0.57.8_8"}
    exit

    export PKG_CACHE_PATH="$(mktemp -d)"
    mkdir -p $PKG_CACHE_PATH/${pkg-tag}
    ln -s ${pkg-fetch} $PKG_CACHE_PATH/${pkg-tag}/fetched-v${pkg-node-version}-${target-os'}-x64
    pkg $build_dir/ubuntu-server.js -t node10-${target-os'}-x64 --out-path $out/${target-os'}
    #rm -rf $build_dir $PKG_CACHE_PATH
  '';

  meta = with stdenv.lib; {
    license = licenses.gpl3;
    maintainers = [ maintainers.pombeirp ];
    platforms = platforms.linux ++ platforms.darwin;
  };
}
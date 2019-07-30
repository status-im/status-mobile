{ stdenvNoCC, fetchurl, jsc-filename }:

let
  # These versions should match node_modules/react-native/ReactAndroid/gradle.properties
  react-native-deps-sources = [
    rec {
      name = "boost";
      version = "1.63.0";
      url = "https://github.com/react-native-community/boost-for-react-native/releases/download/v${version}-0/${name}_${builtins.replaceStrings ["."] ["_"] version}.tar.gz";
      sha256 = "1hq5h6wzcr3sk1kccfk769d59swwxy1kvfhqd6p7786a1zsavdr6";
    }
    rec {
      name = "double-conversion";
      version = "1.1.6";
      url = "https://github.com/google/${name}/archive/v${version}.tar.gz";
      sha256 = "0ynnckpyyhpwisb976knk9gr8jklfwr9flic8xj5flc8iv6hm1bb";
    }
    rec {
      name = "folly";
      version = "2018.10.22.00";
      url = "https://github.com/facebook/${name}/archive/v${version}.tar.gz";
      sha256 = "08cxc8hw34vnyyq45d04spy4dilrgqrv374lwxhg3sx60ww640y4";
    }
    rec {
      name = "glog";
      url = "https://github.com/google/${name}/archive/v0.3.5.tar.gz";
      sha256 = "1q6ihk2asbx95a56kmyqwysq1x3grrw9jwqllafaidf0l84f903m";
    }
  ];

in stdenvNoCC.mkDerivation {
  name = "reactnative-android-native-deps";
  srcs = builtins.map (d: (fetchurl { inherit (d) url sha256; })) react-native-deps-sources;
  jsc = fetchurl {
    url = "https://registry.npmjs.org/jsc-android/-/${jsc-filename}.tgz";
    sha256 = "1pz1ybgwacpvb6m8zy8h77vd2yvf3gidx4av3clr8bg860vivivi"; 
  };
  phases = [ "unpackPhase" ];
  unpackPhase = ''
    # Unpack all source archives.
    mkdir -p $out/deps
    cd $out/deps
    for i in $srcs; do
      unpackFile "$i"
    done
    cp $jsc $out/deps/${jsc-filename}.tar.gz # do the work of downloadJSC
    cd ..

    export sourceRoot=$out/deps
    echo $sourceRoot
  '';
}

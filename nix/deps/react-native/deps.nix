# These versions should match values in:
# node_modules/react-native/ReactAndroid/gradle.properties
# Currently this is done manually.
[
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
    version = "0.3.5";
    url = "https://github.com/google/${name}/archive/v${version}.tar.gz";
    sha256 = "1q6ihk2asbx95a56kmyqwysq1x3grrw9jwqllafaidf0l84f903m";
  }
]

{ importJSON, fetchFromGitHub, pkgs, nodejs, yarn }:

let
  yarn2nix = import (fetchFromGitHub {
    name = "yarn2nix-source";
    owner = "moretea";
    repo = "yarn2nix";
    rev = "3cc020e384ce2a439813adb7a0cc772a034d90bb";
    sha256 = "0h2kzdfiw43rbiiffpqq9lkhvdv8mgzz2w29pzrxgv8d39x67vr9";
  }) { inherit pkgs nodejs yarn; };
  yarnLock = ../../mobile/js_files/yarn.lock;
  packageJSON = ../../mobile/js_files/package.json;
  packageJSONContent = importJSON packageJSON;

  # Create a yarn package for our project that contains all the dependecies, so that we have a
  # known good node_modules folder that we can use later on
  projectNodePackage = yarn2nix.mkYarnModules rec {
    name = "${pname}-${version}";
    pname = packageJSONContent.name;
    version = packageJSONContent.version;
    inherit packageJSON yarnLock;
    # Replace symlink to deps with copy of real dependencies
    postBuild = ''
      # Fixup symlinks in folder we'll be moving.
      # Basically transform a symlink pointing to ../../../../../../a into ../../../../a 
      symlinks=( $(find $out/deps/${pname}/node_modules/ -lname '../../../../../..*') )
      for sl in ''${symlinks[@]}; do
        newTarget=$(readlink "$sl" | sed -E "s|(\.\./){2}(.*)|\2|")
        ln -snf "$newTarget" "$sl"
      done
      unset sl
      unset symlinks

      # Merge deps with node_modules
      cp -R --no-clobber $out/deps/${pname}/node_modules/. $out/node_modules/
      # Get rid of deps
      rm -rf $out/deps/
      # Get rid of symlink to deps
      rm $out/node_modules/${pname}

      # Ensure the we copied expected dependencies from $out/deps
      [ -d $out/node_modules/react-native-fs ] || exit 1
    '';
  };

in projectNodePackage

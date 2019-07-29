{ lib }:

# This Nix expression allows filtering a local directory by specifying dirRootsToInclude, dirsToExclude and filesToInclude.
# It also filters out symlinks to result folders created by nix-build, as well as backup/swap/generated files

let
  inherit (lib)
    any compare compareLists elem elemAt hasPrefix length min splitString take;

  isPathAllowed = allowedPath: path:
    let
      count = min (length allowedPathElements) (length pathElements);
      pathElements = splitString "/" path;
      allowedPathElements = splitString "/" allowedPath;
      pathElementsSubset = take count pathElements;
      allowedPathElementsSubset = take count allowedPathElements;
    in (compareLists compare allowedPathElementsSubset pathElementsSubset) == 0;

  mkFilter = { dirRootsToInclude, # Relative paths of directories to include
    dirsToExclude ? [ ], # Base names of directories to exclude
    filesToInclude ? [ ], # Relative path of files to include
    filesToExclude ? [ ], # Relative path of files to exclude
    root }:
    path: type:
    let
      baseName = baseNameOf (toString path);
      subpath = elemAt (splitString "${toString root}/" path) 1;
      spdir = elemAt (splitString "/" subpath) 0;

    in lib.cleanSourceFilter path type && (
      (type != "directory" && (elem spdir filesToInclude) && !(elem spdir filesToExclude)) ||
      # check if any part of the directory path is described in dirRootsToInclude
      ((any (dirRootToInclude: isPathAllowed dirRootToInclude subpath) dirRootsToInclude) && ! (
        # Filter out version control software files/directories
        (type == "directory" && (elem baseName dirsToExclude))
    )));

in mkFilter

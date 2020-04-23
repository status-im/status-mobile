# This Nix expression allows filtering a local directory by
# specifying dirRootsToInclude, dirsToExclude and filesToInclude.
# It also filters out symlinks to result folders created by nix-build,
# as well as backup/swap/generated files.

{ lib }:

let
  inherit (lib)
    any range flatten length sublist cleanSourceFilter 
    splitString hasPrefix removePrefix concatStringsSep;
  inherit (builtins) map match;

  mkFilter = {
    # primary path under which all files are included, unless excluded
    root,
    # list of regex expressions to match files to include/exclude
    include ? [ ],
    exclude ? [ ], # has precedence over include
    # by default we ignore Version Control System files
    ignoreVCS ? true,
  }:
    let
      # removes superfluous slashes from the path
      cleanRoot = "${toString (/. + root)}/";
    in path: type:
    let
      # unpack path: "x/y/.*" => ["x" "x/y" "x/y/.*"]
      unpackPath = path:
        let
          tokens = splitString "/" path;
          perms = range 1 (length tokens);
          subPaths = builtins.map (x: sublist 0 x tokens) perms;
        in builtins.map (x: concatStringsSep "/" x) subPaths;
      # accept subdirs from regexes
      includeSubdirs = regexes: flatten (map unpackPath regexes);
      # checks all regexes in a list against str
      matchesRegexes = str: regexes: (map (r: (match r str)) regexes);
      # match returns empty list on match
      isMatch = x: x == [ ];
      # path relative to search root
      relPath = removePrefix cleanRoot path;
      # check if any of the regexes match the relative path
      checkRegexes = regexes: any isMatch (matchesRegexes relPath regexes);

      # main check methods
      isRootSubdir = hasPrefix cleanRoot path;
      isIncluded = checkRegexes (includeSubdirs include);
      isExcluded = checkRegexes exclude;
      isVCS = ignoreVCS && !cleanSourceFilter path type;

    in
      if !isRootSubdir then
        # everything outside of root is excluded
        false
      else if isExcluded || isVCS then
        # isExcluded has precedence over isIncluded
        false
      else
        isIncluded;

in mkFilter

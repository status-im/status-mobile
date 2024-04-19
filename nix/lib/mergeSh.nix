# this is an utility for mergeing shells created with mkShell
# TODO: make this an attribute of mkShell result set. Or just use inputsFrom.
{ lib }:

super: shells:
super.overrideAttrs (super:
  let
    inherit (lib) attrByPath unique catAttrs concatStrings;

    mergeAttrsFor = attrName:
      (attrByPath [attrName] [] super) ++ unique (catAttrs attrName shells);
  in {
    inputsFrom = mergeAttrsFor "inputsFrom";
    buildInputs = mergeAttrsFor "buildInputs";
    nativeBuildInputs = mergeAttrsFor "nativeBuildInputs";
    propagatedBuildInputs = mergeAttrsFor "propagatedBuildInputs";
    propagatedNativeBuildInputs = mergeAttrsFor "propagatedNativeBuildInputs";
    # shellHook is a string, not a list
    shellHook = (super.shellHook or "") + concatStrings (catAttrs "shellHook" shells);
  })

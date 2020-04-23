# this is an utility for mergeing shells created with mkShell
# TODO: make this an attribute of mkShell result set.
{ lib }:

super: shells: super.overrideAttrs(super: with lib; {
  inputsFrom = (super.inputsFrom or []) ++ unique (catAttrs "inputsFrom" shells);
  buildInputs = (super.buildInputs or []) ++ unique (catAttrs "buildInputs" shells);
  shellHook = (super.shellHook or "") + concatStrings (catAttrs "shellHook" shells);
})

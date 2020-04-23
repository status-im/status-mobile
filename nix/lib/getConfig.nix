#
# helper for getting status-im config values in a safe way
#

{ lib, config }:

let
  inherit (lib) splitString attrByPath;
in
  name: default:
    let path = [ "status-im" ] ++ (splitString "." name);
    in attrByPath path default config

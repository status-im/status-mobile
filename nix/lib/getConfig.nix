# helper for getting status-im config values in a safe way
#

{ lib, config }:

let inherit (lib) splitString attrByPath;
in name: default:
let
  path = [ "status-im" ] ++ (splitString "." name);
  value = attrByPath path default config;
in if value != null then value else default

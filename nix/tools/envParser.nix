# This Nix expression takes care of reading/parsing the correct .env configuration file and return it as an attr set
{ config, stdenv, lib }:

let
  inherit (builtins) listToAttrs head tail readFile;
  inherit (lib) attrByPath filter hasPrefix nameValuePair splitString;

  build-type = attrByPath ["status-im" "build-type"] "" config;
  ci = (attrByPath ["status-im" "ci"] "" config) != "";

  readLinesFromFile =
    file:
      let
        lines = splitString "\n" (readFile file);
        removeComments = filter (line: line != "" && !(hasPrefix "#" line));
        meaningfulLines = removeComments lines;
      in
        meaningfulLines;
  readFlagsFromFile =
    file:
      let
        lines = readLinesFromFile file;
        genAttrs = lines:
          listToAttrs (map (line:
            let flag = splitString "=" line;
            in nameValuePair (head flag) (head (tail flag))) lines);
      in
        genAttrs lines;
  envFileName =
    if build-type == "release" then ../../.env.release else
    if build-type == "nightly" then ../../.env.nightly else
    if build-type == "e2e"     then ../../.env.e2e else
    if build-type == "pr"      then ../../.env.jenkins else ../../.env;
  flags = readFlagsFromFile envFileName; # TODO: Simplify this path search with lib.locateDominatingFile

in flags

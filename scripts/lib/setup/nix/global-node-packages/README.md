# Abstract

The purpose of this folder is to allow some tools such as `react-native` to be installed globally through Nix instead of resorting to `npm install -g` (which is not allowed in a Nix environment). We get around that by using the [node2nix tool](https://github.com/svanderburg/node2nix).

## Inputs

The inputs are listed in `node-packages.json`.

## Generating the nix expressions

``` shell
node2nix --nodejs-10 --bypass-cache -i node-packages.json
```
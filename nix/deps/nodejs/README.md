# Description

This derivation uses a [yarn2nix](https://github.com/nix-community/yarn2nix) to generate a Nix store package containing all NodeJS dependencies.

# Details

If you look at [`nix/deps/nodejs/default.nix`](./default.nix) you'll see that it it loads `package.json` and `yarn.lock` files and uses them to generate the derivation:
```sh
 > make nix-repl
Welcome to Nix version 2.3.9. Type :? for help.

nix-repl> pkgs.deps.nodejs
«derivation /nix/store/23mpmpjjnq7miclv6bc6ilgypy8wz69p-status-react-node-deps-1.8.0.drv»
```
You can build the derivation:
```
nix-repl> :b pkgs.deps.nodejs    

this derivation produced the following outputs:
  out -> /nix/store/dqb2cjyz1g6n7jic07058y26lnmgaaz9-status-react-node-deps-1.8.0
```
And look inside:
```
 > cd /nix/store/dqb2cjyz1g6n7jic07058y26lnmgaaz9-status-react-node-deps-1.8.0
 > ls
deps  node_modules
 > ls node_modules | grep react-native-image
react-native-image-crop-picker
react-native-image-resizer
react-native-image-viewing
```

# Known Issues

It's important that dependencies from GitHub are added in a correct format:
```
git+https://github.com/status-im/bignumber.js.git#v4.0.2-status
```
Notice three things:

* `git+` prefix for the `https://` URL
* Repository name ends with `.git`
* Tag or branch is added after `#` character

__WARNING__: Using branches can cause dependencies to change their hash and cause builds to fail. Using them during development is fine, but tags should be used in the final version.

If this format is not used correctly you can see issues like:
```
fatal: unable to access 'https://github.com/status-im/bignumber.js.git/': Could not resolve host: github.com
```
or
```
error Couldn't find any versions for "bignumber.js" that matches "github.com/status-im/bignumber.js.git#v4.0.2-status" in our cache (possible versions are ""). This is usually caused by a missing entry in the lockfile, running Yarn without the --offline flag may help fix this issue.
```
or
```
error Can't make a request in offline mode ("https://codeload.github.com/status-im/bignumber.js/tar.gz/f322b670969512a35c84441036a0ba4836a96428")
```

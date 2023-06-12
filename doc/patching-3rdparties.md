# Patching 3rd party dependencies

If 3rd party dependency has an issue and fix is not yet released (or we can't switch to a new release), local patch can be applied. 
Status doesn't used nodejs-way of applying patches. Instead we use Nix.

Patches should be added to [this file](https://github.com/status-im/status-mobile/blob/develop/nix/deps/nodejs-patched/default.nix).

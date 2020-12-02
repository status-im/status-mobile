# Description

This Nix derivation takes the result of the [`nix/deps/nodejs/default.nix`](../nodejs/default.nix) derivation and adjusts it for use with Gradle.

# Details

Modules provided by `yarn2nix` are normally fine, but we use `react-native-*` packages which have their own `gradle.build` files that reference external Maven repositories:
```js
repositories {
    google()
    jcenter()
}
```
And these need to be patched and replaced with `mavenLocal()` to make sure Gradle doesn't try to fetch dependencies from remote repos.

This derivation symlinks most of the modules found in the `yarn2nix` result and copies the ones that require patching of `gradle.build` files in `patchGradlePhase`.

It also applies other fixes like making `BuildId` static in `patchBuildIdPhase` and fixing a Hermes bug in `patchHermesPhase`.

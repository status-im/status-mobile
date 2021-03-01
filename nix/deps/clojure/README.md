# Description

This folder contains the scripts and Nix derivations necessary to generate Clojure dependencies for this project.

# Usage

Running [`generate.sh`](./generate.sh) in a shell that has available `shadow-cljs` and `clojure` should update the `deps.list` and `deps.json` files.

# Details

By using the following command:
```sh
shadow-cljs classpath --force-spawn
```
We both download the necessary JARs and POMs into `~/.m2` folder, but also get the classpath printed into standard output.

We then use the classpath in combination with contents of `~/.m2` folder to generate the following files:

* `deps.list` - List of JARs relative to the `~/.m2` cache folder.
* `deps.json` - Full list of JARs and POMs including their SHAs.

The `deps.list` file is just intermediate and for debugging purposes.
The `deps.json` is loaded by the derivation in [`default.nix`](./default.nix) and used to produce a derivation that contains all the necessary dependencies:

```
 > grep cljfmt nix/deps/clojure/deps.list
cljfmt/cljfmt/0.6.7/cljfmt-0.6.7.jar

 > nix-build --no-out-link -A pkgs.deps.clojure
/nix/store/d7p8r9vhq6z2jmxwakxpcb262wgkc86s-status-react-clojure-deps

 > DEPS=$(nix-build --no-out-link -A pkgs.deps.clojure)                              

 > find $DEPS -iname 'cljfmt-0.6.7.*'
/nix/store/d7p...86s-status-react-clojure-deps/cljfmt/cljfmt/0.6.7/cljfmt-0.6.7.pom.sha1
/nix/store/d7p...86s-status-react-clojure-deps/cljfmt/cljfmt/0.6.7/cljfmt-0.6.7.pom
/nix/store/d7p...86s-status-react-clojure-deps/cljfmt/cljfmt/0.6.7/cljfmt-0.6.7.jar.sha1
/nix/store/d7p...86s-status-react-clojure-deps/cljfmt/cljfmt/0.6.7/cljfmt-0.6.7.jar
```

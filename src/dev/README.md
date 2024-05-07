# The `dev/` directory

The `src/dev/` directory is a place where developers can commit non-production
and non-test code which can facilitate development in general. The code should,
ideally, be useful to more than one team member.

**Note**: As of today (2024-05-07), we don't have further guidelines or examples
about how mobile teams should use and maintain these dev-only namespaces. Before
opening a PR introducing new dev-only namespaces, consider sharing and
discussing with as many team members as you can.

### The `user` namespace

You may optionally create a `src/dev/user.cljs` file. This file is git-ignored
and it is meant to be your own playground. Clojure developers usually use this
sort of namespace to create temporary utilities and experiments that aren't
necessarily useful to others.

Be careful with `make clean` because it will delete all non-tracked git files.
If you rely on this file, you may prefer to create a symbolic link and store the
original `user.cljs` file outside the `status-mobile` repository. Then, after
`make clean`, you will need to recreate the symlink.

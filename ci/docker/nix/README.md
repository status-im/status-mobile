# Description

In order to make Nix work with Jenkins we have to make it possible to use it as Jenkins user.

For that reason we add `jenkins` user with the right UID and GID matching the ones on CI hosts.

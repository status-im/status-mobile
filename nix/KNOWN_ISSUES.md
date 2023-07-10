# Known Issues

## Ignoring Untrusted Substituter on NixOS

When using our setup on NixOS users can see this warning:
```
warning: ignoring untrusted substituter 'https://nix-cache.status.im/', you are not a trusted user.
```
Which is due to incorrect `trusted-users` setting:
```
> grep trusted-users /etc/nix/nix.conf
trusted-users = root
```
Which can be extended using the [`nix.settings.trusted-users`](https://search.nixos.org/options?channel=23.05&show=nix.settings.trusted-users) option.

## Too many open files

### Single-User Installation

Nix can open a lot of files when fetching things for `/nix/store` which can cause
```
Too many open files"
```
The temporary way to fix this is set a new limit for current session using `ulimit`:
```sh
ulimit -n 131072
```
To increase limit permanently system-wide edit `/etc/sysctl.conf`:
```
fs.file-max = 131072
```
And use `sudo sysctl --system` to load these new settings.

### Multi-User Installation

This has been fixed in Nix `2.9.0`:

* https://github.com/NixOS/nix/issues/6007
* https://github.com/NixOS/nix/pull/6553

## Cache Downloads Timing Out

If copying from Nix Cache times out you can adjust the timeout by changing [`nix/nix.conf`](/nix/nix.conf):
```conf
stalled-download-timeout = 9001
```

## NDK Bundle Download Failures

There is a known issue([#11111](https://github.com/status-im/status-mobile/issues/11111)) where the fetching of Android NDK Bundle from our own cache host fails in various different ways:
```
unable to download 'https://nix-cache.status.im/nar/64g5wharwjj040rg1v8jnc5qhv1hkgds.nar':
  HTTP error 200 (curl error: Stream error in the HTTP/2 framing layer)
```
```
unable to download 'https://nix-cache.status.im/nar/64g5wharwjj040rg1v8jnc5qhv1hkgds.nar':
  HTTP error 200 (curl error: Transferred a partial file)
```
```
NAR for '/nix/store/psx79cnz1khfjlzkw3j8x9sb1jhk1v1a-ndk-bundle-21.0.6113669' fetched from 'https://nix-cache.status.im' is incomplete
```
Currently the simplest temporary solution is to edit `nix/nix.conf` and comment out the `extra-substituters` line.
This should allow Nix to download the NDK Bundle directly from Google servers rather than from our own cache.

## `extra-sandbox-paths` Is a Restricted Setting

When building Android on NixOS you might encounter the following error:
```
ignoring the user-specified setting 'extra-sandbox-paths', because it is a restricted setting and you are not a trusted user
```
You can mitigate this by setting the [`nix.trustedUsers`](https://nixos.org/nixos/options.html#nix.trustedusers) property.

## NixOS Prioritizes System Config

Currently on NixOS `NIX_CONF_DIR` is being ignored in favor of the default `/etc/nix/nix.conf`.
This will be possible to fix once Nix `2.4` comes out with support for `NIX_USER_CONF_FILES`.

For more details see https://github.com/NixOS/nix/issues/3723.

## Extra Sandbox Files Not Available

It's possible that on a multi-user installation files provided to the build sandbox using the `extra-sandbox-paths` option will ne be available, unless the current user is added to `trusted-users` in `nix.conf` file.

Issues:
* https://github.com/NixOS/nix/issues/6115
* https://github.com/NixOS/nix/issues/6217

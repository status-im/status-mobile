# Known Issues

## MacOS 10.15 "Catalina"

There is an unsolved issue with the root(`/`) file system in `10.15` being read-only:
https://github.com/NixOS/nix/issues/2925

Our current recommended workaround is putting `/nix` under `/opt/nix` and symlinking it via `/etc/synthetic.conf`:

```bash
sudo mkdir /opt/nix
sudo chown ${USER} /opt/nix
sudo sh -c "echo 'nix\t/opt/nix' >> /etc/synthetic.conf"
reboot
```

After the system reboots you should see the `/nix` symlink in place:

```bash
 % ls -l /nix
lrwxr-xr-x  1 root  wheel  8 Oct 11 13:53 /nix -> /opt/nix
```

In order to be able to use Nix with a symlinked `/nix` you need to include this in your shell:

```bash
export NIX_IGNORE_SYMLINK_STORE=1
```

Add it to your `.bashrc` or any other shell config file.

__NOTE__: Your old `/nix` directory will end up in `/Users/Shared/Relocated Items/Security/nix` after OS upgrade.

## Cache Downloads Timing Out

If copying from Nix Cache times out you can adjust the timeout by changing [`nix/nix.conf`](/nix/nix.conf):
```conf
stalled-download-timeout = 9001
```

## NDK Bundle Download Failures

There is a known issue([#11111](https://github.com/status-im/status-react/issues/11111)) where the fetching of Android NDK Bundle from our own cache host fails in various different ways:
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

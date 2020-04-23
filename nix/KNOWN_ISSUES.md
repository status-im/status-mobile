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

## `extra-sandbox-paths` Is a Restricted Setting

When building Android on NixOS you might encounter the following error:
```
ignoring the user-specified setting 'extra-sandbox-paths', because it is a restricted setting and you are not a trusted user
```
You can mitigate this by setting the [`nix.trustedUsers`](https://nixos.org/nixos/options.html#nix.trustedusers) property.

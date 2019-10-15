# Description

This folder contains configuration for [Nix](https://nixos.org/), a purely functional package manager used by the Status app for its build process.

# Configuration

The main config file is [`nix/nix.conf`](/nix/nix.conf) and its main purpose is defining the [binary caches](https://nixos.org/nix/manual/#ch-basic-package-mgmt) which allow download of packages to avoid having to compile them yourself locally.

__NOTE:__ If you are in Asia you might want to add the `https://nix-cache-cn.status.im/` to be first in order of `substituters`. Removing `cache.nixos.org` could also help.

# Shell

In order to access an interactive Nix shell a user should run `make shell`.

The Nix shell is started in this repo via the [`nix/shell.sh`](/nix/shell.sh) script, which is a wrapper around the `nix-shell` command and is intended for use with our main [`Makefile`](/Makefile). This allows for an implicit use of `nix-shell` as the default shell in the `Makefile`.

By default the shell starts without any specific target platform, if you want to change that you should export the `TARGET_OS` env variable with the right value:

```bash
make shell TARGET_OS=android
```
This way your shell and all other nix commands should run in a setup that is tailored towards Android development.

For valid values you can check the [`nix/platform.nix`](/nix/platform.nix) file.

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
```
 % ls -l /nix
lrwxr-xr-x  1 root  wheel  8 Oct 11 13:53 /nix -> /opt/nix
```
In order to be able to use Nix with a symlinked `/nix` you need to include this in your shell:
```bash
export NIX_IGNORE_SYMLINK_STORE=1
```
Add it to your `.bashrc` or any other shell config file.

__NOTE__: Your old `/nix` directory will end up in `/Users/Shared/Relocated Items/Security/nix` after OS upgrade.

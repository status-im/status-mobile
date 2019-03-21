#!/bin/sh

# This script installs the Nix package manager on your system by
# downloading a binary distribution and running its installer script
# (which in turn creates and populates /nix).

{ # Prevent execution if this script was only partially downloaded
oops() {
    echo "$0:" "$@" >&2
    exit 1
}

tmpDir="$(mktemp -d -t nix-binary-tarball-unpack.XXXXXXXXXX || \
          oops "Can't create temporary directory for downloading the Nix binary tarball")"
cleanup() {
    rm -rf "$tmpDir"
}
trap cleanup EXIT INT QUIT TERM

require_util() {
    type "$1" > /dev/null 2>&1 || command -v "$1" > /dev/null 2>&1 ||
        oops "you do not have '$1' installed, which I need to $2"
}

case "$(uname -s).$(uname -m)" in
    Linux.x86_64) system=x86_64-linux; hash=e229e28f250cad684c278c9007b07a24eb4ead239280c237ed2245871eca79e0;;
    Linux.i?86) system=i686-linux; hash=223c141b7ce322aacd4003dfc79b969ee813396d2ac586cf44c0e787233b1666;;
    Linux.aarch64) system=aarch64-linux; hash=94d3d7d2b551b228ec38f83c6f037c5e5b6aaf0805450967f800c02a827b1f41;;
    Darwin.x86_64) system=x86_64-darwin; hash=4182a38948c9ce520993f79a5caf8ce3d49636189f4f0f7e8decbc91ddb61cd1;;
    *) oops "sorry, there is no binary distribution of Nix for your platform";;
esac

url="https://nixos.org/releases/nix/nix-2.2.1/nix-2.2.1-$system.tar.bz2"

tarball="$tmpDir/$(basename "$tmpDir/nix-2.2.1-$system.tar.bz2")"

require_util curl "download the binary tarball"
require_util bzcat "decompress the binary tarball"
require_util tar "unpack the binary tarball"

echo "downloading Nix 2.2.1 binary tarball for $system from '$url' to '$tmpDir'..."
curl -L "$url" -o "$tarball" || oops "failed to download '$url'"

if type sha256sum > /dev/null 2>&1; then
    hash2="$(sha256sum -b "$tarball" | cut -c1-64)"
elif type shasum > /dev/null 2>&1; then
    hash2="$(shasum -a 256 -b "$tarball" | cut -c1-64)"
elif type openssl > /dev/null 2>&1; then
    hash2="$(openssl dgst -r -sha256 "$tarball" | cut -c1-64)"
else
    oops "cannot verify the SHA-256 hash of '$url'; you need one of 'shasum', 'sha256sum', or 'openssl'"
fi

if [ "$hash" != "$hash2" ]; then
    oops "SHA-256 hash mismatch in '$url'; expected $hash, got $hash2"
fi

unpack=$tmpDir/unpack
mkdir -p "$unpack"
< "$tarball" bzcat | tar -xf - -C "$unpack" || oops "failed to unpack '$url'"

script=$(echo "$unpack"/*/install)

[ -e "$script" ] || oops "installation script is missing from the binary tarball!"
"$script" "$@"

} # End of wrapping

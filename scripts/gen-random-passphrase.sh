#!/usr/bin/env bash

if [ `uname` == 'Darwin' ]; then
  echo "
Uh oh. We seem to be on macOS where I don't know how to bypass the
PRNG and can't guarantee the entropy of the data. Run this on Linux or
BSD, or use 'pwgen --secure 86' instead if using a PRNG is acceptable.
"
  exit 1
fi

echo "
Generating random passphrases with 512 bits of entropy and a length
of 86 characters, intended for publicly hosted private keys. This is
total overkill even for AES-256, but we want some headroom if the RNG
is less than perfect and they aren't meant for human memorisation.

If this hangs, move the mouse around, type into a different window and do
some disk and network I/O to generate additional noise for the entropy pool.

When done, intersperse some random characters of your choice throughout the
passphrases.
"
for PASS in INNER OUTER KEYCHAIN; do
  echo "$PASS"
  head -c 64 /dev/random | base64 | head -c 86
  echo -e "\n"
done

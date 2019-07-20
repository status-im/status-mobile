#!/usr/bin/env nix-shell
#! nix-shell -i bash -p bash ruby bundler bundix

rm -f Gemfile.lock
bundler install --path .bundle/vendor
bundix
rm -rf .bundle/vendor

if [ "clean" == "$1" ]; then
  rm -r ~/.gem
fi

#!/usr/bin/env bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <input_image.png>"
  exit 1
fi

input_file="$1"
backup_file="${input_file%.png}.bak.png"
output_file="$input_file"

cp "$input_file" "$backup_file"

magick "$input_file" -quality 100 -define png:compression-level=9 -strip -colors 256 "$output_file"

if [ $? -eq 0 ]; then
  echo "Image compression completed: $input_file"
  echo "Backup created: $backup_file"
else
  mv "$backup_file" "$input_file"
  echo "Error during image compression"
  exit 1
fi

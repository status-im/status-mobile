# Helper for verifying an environment variable is set
name: ''
  if [[ -z ''$${name} ]]; then 
    echo 'Not env var set: ${name}' >&2
    exit 1
  fi
''

# Helper for verifying an environment variable is set
name: ''
  if [[ -z ''$${name} ]]; then 
    echo 'WARNING! Env var not set: ${name}' >&2
  fi
''

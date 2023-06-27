name: default: let
  envOverride = builtins.getEnv name;
  logEnvOverride = value:
    builtins.trace "getEnvWithDefault ${name} (default: ${toString default}): ${value}" value;
in
  if envOverride != ""
  then logEnvOverride envOverride
  else default

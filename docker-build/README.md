# Build

```
docker-compose -f docker-build/docker-compose.yml build
```

This will install all the required sdks, depending on the connection will take some time.

# Run

```
docker-compose -f docker-build/docker-compose.yml up
```

This will start `adbd` and `fighwheel` listening on port `4567`.

You need to connect your device and accept the key.

After the figwheel prompt you can install the application running:

```
docker-compose -f docker-build/docker-compose.yml exec adbd make run-android
```

# Notes

## Adbd key

The adbd key is generated every time you start/stop the container, so you will have to accept it if that happens.

## Security

This will give the docker image access to your USB devices and run in priviliged mode, so run at your own risk.

## Multiple adbd

You will need to make sure no `adbd` is running on your local machine, as that would interfer with the one running in docker.

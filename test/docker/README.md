Status react protocol tests
=====================

## Run the tests

To run the tests with `docker-compose`, execute:
```
docker-compose run tests
```

> It may be required to make your node a swarm manager first. If you see an appropriate error, follow the instructions.

This command will start a bootnode and build a Whisper node running the latest `status-go`.

For more information about bootnodes see https://github.com/ethereum/go-ethereum/wiki/Setting-up-private-network-or-local-cluster

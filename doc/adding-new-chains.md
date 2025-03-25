# Adding new chains

This doc only describes how to add new chains on the client side, so make sure the chain is added on `status-go` first (see [eth-rpc-proxy:ADD_NEW_CHAINS.md](https://github.com/status-im/eth-rpc-proxy/blob/master/ADD_NEW_CHAINS.md))

## Steps
1. Add the chain image (.png) to `resources/images/networks/`
2. Add the chain name (keyword) and the path to the chain image to `quo.foundations.resources/networks`
3. Add the mainnet (and/or testnet) `chain-id` constant(s) to `status-im.contexts.wallet.networks.config`
4. Add additional chain information in the same namespace, under `mainnets` and `testnets` respectively, including the chain name keyword added for the image.
5. In case the network should be highlighted as _new_ in the UI, add the chain id to `status-im.contexts.wallet.networks.config/new-networks`

## Future steps to improve addition of new chains and facilitate the addition of custom chains by the user
1. Remove reliance on the `network-name` and refer to a chain solely by its `chain-id`
2. Remove the definition of the network images from `quo.foundations.resources/networks` by relying on the images provided by `status-go`
3. `quo` components that should render a network image should get the image source as a prop
4. The chain data added in `status-im.contexts.wallet.networks.config` should be received from `status-go` or derived on the client, instead of being added manually.
5. Ideally, when adding a new chain, we shouln't bring new changes on the client. All the changes should happen on the `status-go` side
6. Remove deprecated namespaces dealing with chains e.g. `utils.ethereum.chains`

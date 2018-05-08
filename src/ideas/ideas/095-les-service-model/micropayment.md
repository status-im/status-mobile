## Micropayment

### Existing Solutions

#### Raiden

##### Concept

- Off-chain solution for ERC20-compliant token transfers on Ethereum
- Designed for many-to-many solutions
- Transfer of tokens without global consensus
- Previously setup on-chain deposits and *balance proofs*
- Payment channels for bidirectional transfers between two participants as long as deposit isn't exceeded
- Transfers require no fees
- Funds can be added to existing channels
- Blockchain isn't involved during transfers, only for channel creation and potential closing
- As binding as on-chain due to smart contract containing tokens only accessable to both participants
- Closing leads to payments to both participants based on balance
- *Network* of channels with routing allow transfers between all peers, secured by *hash-locks*
- Scaling with the number of peers
- RESTful API

##### Links

- https://raiden.network/
- http://raiden-network.readthedocs.io/en/stable/
- https://medium.com/@raiden_network/

#### µRaiden

##### Concept

- Like Raiden but only between two parties: *sender* (client) and *receiver* (service provider)
- Designed for many-to-one solutions
- Creation of channel and settling leads to µRaiden Smart Contract and a *deposit*
- Smart Contract enforces payment of deposit
- Signing payments with balance-proofs between sender and receiver allows off-chain transactions
- Once channel gets closed deposit is payed to receiver based on *balance proofs* and rest to sender
- Typically only two on-chain transactions for opening and closing
- If deposit of sender gets low it can increase it with an on-chain transaction
- Own ERC20 and ERC223 compliant token
- RESTful API

##### Links

- https://raiden.network/micro.html
- https://github.com/raiden-network/microraiden
- https://microraiden.readthedocs.io/en/docs-develop/

#### SWAP

##### Concept

- Swarm Accounting Protocol
- Generic *peerwise* accounting
- Long-term exchange of data chunks balanced by data chunks preferred
- Relies on reputation system
- Open for payment systems with issue and receive function
- Issue results in a 3rd party proveable promise of payment
- Issued *cheques* will be accumulated in *cheque book*
- Only last one will be cashed in
- Peer decides how often to cash in
- Transactions costs motivate to minimize their number
- Trust to peers grows with good behavior
- SWAP^2 provides API to set triggers for automatic payments
- SWAP^3 reduces transaction cost by debt swap

##### Links

- https://github.com/ethersphere/swarm/wiki/Swap
- https://github.com/ethersphere/swarm/wiki/Swap-demo-instructions

#### Plasma [Cash]

##### Concept

- Child-chains with PoA for ERC20/ERC223-compliant tokens
- Needs the plasma chain block headers to be written to root chain
- Submit the plasma chain blocks to the root chain on its every block
- Any number of users in a plasma side chain
- The root chain is involved into transaction for creating a side-chain, adding funds (deposit) and withdraws
- Plasma: withdraw takes 7 days for confirmation
- Plasma Cash: withdraw without any confirmation
- Plasma Cash: the plasma chain + ERC721 unique identifiers for each token
- Plasma Cash: sharded client-side validation
- The implementation is going to be integrated to the Ethereum blockchain
- If a block producer starts acting maliciously, a user can leave the child-chain with his or her tokens. The user can publish a fraud proof to the root contract (valid previous and current states + invalid fraud state) and withdraw his or her tokens
- Invalid exits can be challenged by any user with the token's data

##### Links

- https://karl.tech/plasma-cash-simple-spec
- https://github.com/ethereum-plasma
- https://ethresear.ch/t/minimal-viable-plasma/426
- https://www.youtube.com/channel/UCG2MeKuKDJRK4gFNk-dQuZQ
- https://plasma.io
- https://github.com/BANKEX/PlasmaETHexchange

### API

#### Client

##### Query Conditions

Ask service provider about pricing and model (bandwidth, CPU, etc.).

##### Establish Business Contract

Establish a payed connection with an initial deposit.

##### Check Balance

Check balance of the deposit to increase fund early enough before
connection potentially is dropped.

##### Increase Fund

Increase fund to keep connection with higher quality level. If
fund is spent connection may not be dropped but changed into
free connection. That may be dropped later.

##### Close Contract

Close contract so that used deposit is spent service provide and
rest is refunded.

#### Service Provider

##### Book Token

Book a token based on provided service. If all tokens of deposit
are used with a booking then close contract.

##### Close Contract

Close contract to retrieve used deposit and transform connection
into a free connection.


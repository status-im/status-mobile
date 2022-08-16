# 0013. Tribute-to-Talk

| Date | Tags |
|---|---|
| 2019-04-02 | spam filtering, blocking, chat |


## Status

proposed

## Context

The whitepaper describes Tribute to Talk (TtT) as an economics-based anti-spam filter for receiving messages and "cold" contact requests from user. Stakeholders would stake a minimum amount of SNT that another Status stakeholder must deposit in order to contact him directly.
The whitepaper also proposes that the deposit is only forfeited to the recipient if he replies.

## Decision

Considering:
- the absence of efficient ways to perform anonymous transactions (zk-snarks could be used in the future for that)
- the impossibility to prove that a recipient has made an actual reply and not some kind of automated reply (captcha solution was proposed, but wouldn't be pratical until we can use a solution such as swarm feeds that allow users to make free updates to their captcha without on-chain transactions)
- the limited time to develop the feature

We opted for a solution that:
- doesn't establish a direct correlation between the tribute payment and an on-chain transaction, so it
- uses regular transactions for tributes payment, giving users plausible deniability about the nature of the payment
- doesn't store tribute information on-chain

## Manifests

Since TtT related informations aren't stored on chain, they need to be stored somewhere else.
For the first iteration of TtT we opted for IPFS, which is already used accross the app for other forms of content, through Infura IPFS gateway.

On IPFS we store what we call a ttt manifest, which is a json file with the following format:

```json
{
 "tribute-to-talk": 
    "snt-amount": "snt amount in wei that a user has to pay as tribute",
    "message": "a personalized message to display to users trying to contact stakeholder"
}
```

When a user sets TtT in his profile, the manifest contenthash is added to the registry contract (which requires a transaction).
The TtT contract points ethereum addresses to contenthash manifests. In the current implementation, the manifest is not encrypted, but we think it would be a privacy improvement to have symmetric key encryption for the manifest using the user public-key. That way, parsing the contract event log is useless to collect TtT manifests because there is no way to find the public key from the address, but legit users will know the public-key already.

### Future implementation

We were considering Swarm feeds to upload manifests but we were worried about the current instabilities, particularly a bug that was returning stale reads very often and made it unsuitable for our usage.

Swarm feeds are the ideal solution as they allows users to set a manifest without any transaction which makes the registry contract completely obsolete.
The format of the manifest will stay the same, but could be extended with other top-level keys to serve as a public profile as well.

## Implementation

### Checking manifest

In status app, we check a user manifest when:
- the app starts (user checks his own manifest)
- a user's profile is opened
- a chat with a user is opened
If there is no match in the registry for the user address or if the manifest is not available, the client will consider that there is no tribute to talk set for the user.

Relevant code is located in `status-im.tribute-to-talk.core` ns

### Whitelisting rules

A is whitelisted by B:
- A received a contact request from B
- A has paid the tribute to B
- B has paid the tribute to A

A whitelists B if:
- A sent a contact request to B
- A has paid the tribute to B
- B has paid the tribute to A

### Paying tribute

The tribute payment is a regular SNT transaction greater or equal to the value of the snt-amount key in the manifest.

The id of the transaction is sent along messages in the payload top level `:tribute-transaction` key as long as the sender isn't sure that he is whitelisted by the recipient (see `whitelisting rules` above).

The UI in status app waits for 1 confirmation on chain before allowing the user to send a message to ensure the recipient will see the payment and not ignore the message.

## Upgradability

The registry contract has a `stopped` attribute that needs to be checked on start-up by clients. If the contract is stopped, this means that a new version of tribute to talk that is incompatible with the current one has been released.

Old clients MUST check if the tribute to talk contract is stopped and MUST disable tribute to talk if it is.
Old clients MUST warn the user when they open a chat with a user that may have not whitelisted them that their messages will be ignored if the recipient has tribute to talk enabled.

New client will be using a different contract

## Consequences

### Data collection of manifests' data

- if we don't encrypt the manifests with the users public key, the registry contract logs can be used to collect manifest contenthashes which can then be downloaded and the data aggregated
- SNT transactions could then be correlated to have an estimate of which SNT transactions are like to be tributes payments (it would give the "attacker" an additional filter on transaction amount, transaction recipient being already known through the registry contract without the manifest)
- this would not be possible with swarm feeds as there wouldn't be a registry contract to look at.

### Impact of token transactions
- currently there is a wallet limitation that limits the token transactions list to the past 2 weeks, this means that if a user pays and doesn't send any message after the payment for 2 weeks, he will silently be ignored.
- this will be solved by fixing the transaction list in the wallet

### Edge cases (implementation specific)

- if A and B have a conversation, and B suddenly sets TtT on, if B didn't add A as a contact, A messages will be filtered and A will not now it until he leaves and returns to the chat

### Switching networks

When switching between networks, the app uses the settings on the current network. This means that if one has TtT enabled on Mainnet and goes on Testnet, and one has no TtT settings on Testnet, TtT will be considered disabled and no messages will be filtered.

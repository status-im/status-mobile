# 0006. Wallet Compatibility

| Date | Tags |
|---|---|
| 2018-05-15 | ethereum, wallet |


## Status

Proposed

## Context

Idea [142-Wallet-Compatibility](https://ideas.status.im/ideas/142-wallet-compatibility/README) changes the way we generate keys starting from the 12 mnemonic words.

This change allows users to create a wallet in other apps and import the same address in Status using the 12 mnemonic words.

### Breaking Changes

The 12 mnemonic words will generate different addresses and whisper identities (public key). We should communicate this properly and recommend users to create new accounts.

### Features

If users used a dapp in an external app/wallet, they would be able to continue to use the same dapp with the same account in Status.

## Decision

* update the BIP39 seed generation to use the salt `"mnemonic"` instead of `"status-im"` following [BIP39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki#from-mnemonic-to-seed).

* update the master key generation using the constant `"Bitcoin seed"` instead of `"status-im"`, following [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki#master-key-generation).

* remove the use of the password in the salt during the BIP39 seed generation (password is not removed from the app in general, and it's still required to encrypt the keys on the device).

### Relevant reading

* [BIP39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki)
* [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki)
* [BIP44](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki)

## Consequences

Given that we changed the algorithm used to generate keys and addresses, it will be impossible to re-import accounts created with the old algorithm in Status.

If you have any token in your old account, you can upgrade to the new version and move them to a new one created with the new algorithm.


### Upgrade example

1. I create account A using an old version of Status.
2. I upgrade Status.
3. I can still log in to account A using my password.
4. I create a new account B.
5. I copy the address of account B to the clipboard.
6. I log out.
7. I log in to account A.
8. I send my tokens to account B.

### Warnings

After step 2, you won't be able to re-import account A.
If you uninstall Status, or you install Status to a different device, and import existing account using the 12 mnemonic words of account A, the address generated will be different from the original one.

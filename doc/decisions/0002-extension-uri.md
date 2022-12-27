# 0002. Extension URI

| Date | Tags |
|---|---|
| 2018-04-26 | extension, uri, ethereum |


## Status

proposed

## Context

Extensions are defined by an EDN file accessed from decentralized storage. An URI schema allows to identify those files.

## Decision

URI follows ethereum URI schema as specified in [EIP 831](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-831.md)


```
uri                     = "ethereum:" path
path                    = "status:extension" ":" storage "@" id
storage                 = STRING (e.g. IPFS)
id                      = STRING (e.g. IPFS hash)
```

An example of an extension available on IPFS is: `ethereum:status:extension:ipfs@QmTeW79w7QQ6Npa3b1d5tANreCDxF2iDaAPsDvW6KtLmfB`

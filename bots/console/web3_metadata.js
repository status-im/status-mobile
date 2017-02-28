var WEB3_UNIT = [
    'kwei/ada',
    'mwei/babbage',
    'gwei/shannon',
    'szabo',
    'finney',
    'ether',
    'kether/grand/einstein',
    'mether',
    'gether',
    'tether'
];

// because web3 doesn't provide params or docs
var DOC_MAP = {
    console: {
        log : {
            desc: 'Outputs a message to chat context.',
            args: [{
                name: 'text',
                type: 'String',
                desc: 'message to output to chat context'
            }]
        }
    },
    web3: {
        // setProvider     : ['provider'], // TODO
        version: {
            api: {
                desc: 'The ethereum js api version.'
            },
            node: {
                desc: 'The client/node version.'
            },
            network: {
                desc: 'The network protocol version.'
            },
            ethereum: {
                desc: 'The ethereum protocol version.'
            },
            whisper: {
                desc: 'The whisper protocol version.'
            },
        },
        isConnected: {
            desc: 'Check if a connection to a node exists.',
            args: []
        },
        currentProvider: {
            desc: 'Will contain the current provider, if one is set. This can be used to check if mist etc. set already a provider.'
        },
        reset: {
            desc: 'Should be called to reset state of web3. Resets everything except manager. Uninstalls all filters. Stops polling.',
            args: [{
                name: 'keepIsSyncing',
                type: 'Boolean',
                desc: 'If true it will uninstall all filters, but will keep the web3.eth.isSyncing() polls'
            }]
        },
        sha3: {
            desc: 'Returns the Keccak-256 SHA3 of the given data.',
            args: [{
                name: 'string',
                type: 'String',
                desc: 'The string to hash using the Keccak-256 SHA3 algorithm'
            }, {
                name: 'options',
                type: 'Object',
                optional: true,
                desc: 'Set encoding to hex if the string to hash is encoded in hex. A leading 0x will be automatically ignored.'
            }]
        },
        toHex: {
            desc: 'Converts any value into HEX',
            args: [{
                name: 'mixed',
                type: 'String|Number|Object|Array|BigNumber',
                desc: 'The value to parse to HEX. If its an object or array it will be JSON.stringify first. If its a BigNumber it will make it the HEX value of a number.'
            }]
        },
        toAscii: {
            desc: 'Converts a HEX string into a ASCII string.',
            args: [{
                name: 'hexString',
                type: 'String',
                desc: 'A HEX string to be converted to ascii.'
            }]
        },
        fromAscii: {
            desc: 'Converts any ASCII string to a HEX string.',
            args: [{
                name: 'string',
                type: 'String',
                desc: 'An ASCII string to be converted to HEX.'
            }, {
                name: 'padding',
                type: 'Number',
                desc: 'The number of bytes the returned HEX string should have. '
            }]
        },
        toDecimal: {
            desc: 'Converts a HEX string to its number representation.',
            args: [{
                name: 'hexString',
                type: 'String',
                desc: 'An HEX string to be converted to a number.'
            }]
        },
        fromDecimal: {
            desc: 'Converts a number or number string to its HEX representation.',
            args: [{
                name: 'number',
                type: 'Number',
                desc: 'A number to be converted to a HEX string.'
            }]
        },
        fromWei: {
            desc: 'Converts a number of wei into an ethereum unit',
            args: [{
                name: 'number',
                type: 'Number|String|BigNumber',
                desc: 'A number or BigNumber instance.'
            }, {
                name: 'unit',
                type: 'string',
                desc: 'One of the ether units'
            }]
        },
        toWei: {
            desc: 'Converts an ethereum unit into wei',
            args: [{
                name: 'number',
                type: 'Number|String|BigNumber',
                desc: 'A number or BigNumber instance.'
            }, {
                name: 'unit',
                type: 'string',
                desc: 'One of the ether units'
            }]
        },
        toBigNumber: {
            desc: 'Converts a given number into a BigNumber instance',
            args: [{
                name: 'numberOrHexString',
                type: 'Number|String',
                desc: 'A number, number string or HEX string of a number.'
            }]
        },
        net: {
            listening: {
                desc: 'Is node actively listening for network connections?'
            },
            peerCount: {
                desc: 'Returns the number of connected peers'
            }
        },
        isAddress: {
            desc: '',
            args: [{
                name: '',
                type: 'string',
                desc: 'hex string'
            }], // TODO not in docs
        },
        eth: {
            defaultAccount: {
                desc: 'The currently set default address'
            },
            defaultBlock: {
                desc: 'The default block number to use when querying a state.'
            },
            syncing: {
                desc: 'Returns the either a sync object, when the node is syncing or false.'
            },
            isSyncing: {
                desc: 'This convenience function calls the callback everytime a sync starts, updates and stops.',
                args: [{
                    name: 'callback',
                    type: 'Function',
                    desc: 'The callback will be fired with true when the syncing starts and with false when it stopped. While syncing it will return the syncing object: {startingBlock, currentBlock, highestBlock}'
                }]
            },
            coinbase: {
                desc: 'Returns the coinbase address were the mining rewards go to.'
            },
            mining: {
                desc: 'Says whether the node is mining or not.'
            },
            hashrate: {
                desc: 'Returns the number of hashes per second that the node is mining with.'
            },
            gasPrice: {
                desc: 'Returns the current gas price. The gas price is determined by the x latest blocks median gas price'
            },
            accounts: {
                desc: 'Returns a list of accounts the node controls'
            },
            blockNumber: {
                desc: 'Returns the current block number'
            },
            getBalance: {
                desc: 'Get the balance of an address at a given block.',
                args: [{
                    name: 'addressHexString',
                    type: 'String',
                    desc: 'The address to get the balance of'
                }, {
                    name: 'defaultBlock',
                    type: 'Number|String',
                    optional: true,
                    desc: 'If you pass this parameter it will not use the default block set with web3.eth.defaultBlock.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getStorageAt: {
                desc: 'Get the storage at a specific position of an address.',
                args: [{
                    name: 'addressHexString',
                    type: 'String',
                    desc: 'The address to get the storage from.'
                }, {
                    name: 'position',
                    type: 'Number',
                    desc: 'The index position of the storage.'
                }, {
                    name: 'defaultBlock',
                    type: 'Number|String',
                    optional: true,
                    desc: 'If you pass this parameter it will not use the default block set with web3.eth.defaultBlock.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getCode: {
                desc: 'Get the code at a specific address.',
                args: [{
                    name: 'addressHexString',
                    type: 'String',
                    desc: 'The address to get the code from.'
                }, {
                    name: 'defaultBlock',
                    type: 'Number|String',
                    optional: true,
                    desc: 'If you pass this parameter it will not use the default block set with web3.eth.defaultBlock.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getBlock: {
                desc: 'Returns a block matching the block number or block hash.',
                args: [{
                    name: 'blockHashOrBlockNumber',
                    type: 'String|Number',
                    desc: 'The block number or hash. Or the string "earliest", "latest" or "pending"'
                }, {
                    name: 'returnTransactionObjects',
                    type: 'Boolean',
                    optional: true,
                    desc: '(default false) If true, the returned block will contain all transactions as objects, if false it will only contains the transaction hashes.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getBlockTransactionCount: {
                desc: 'Returns the number of transaction in a given block.',
                args: [{
                    name: 'hashStringOrBlockNumber',
                    type: 'String|Number',
                    desc: 'The block number or hash. Or the string "earliest", "latest" or "pending"'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getUncle: {
                desc: 'Returns a blocks uncle by a given uncle index position',
                args: [{
                    name: 'blockHashStringOrNumber',
                    type: 'String|Number',
                    desc: 'The block number or hash. Or the string "earliest", "latest" or "pending"'
                }, {
                    name: 'uncleNumber',
                    type: 'Number',
                    desc: 'The index position of the uncle.'
                }, {
                    name: 'returnTransactionObjects',
                    type: 'Boolean',
                    desc: '(default false) If true, the returned block will contain all transactions as objects, if false it will only contains the transaction hashes.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getBlockUncleCount: {
                desc: '', // TODO missing from docs
            },
            getTransaction: {
                desc: 'Returns a transaction matching the given transaction hash.',
                args: [{
                    name: 'transactionHash',
                    type: 'String',
                    desc: 'The transaction hash.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getTransactionFromBlock: {
                desc: 'Returns a transaction based on a block hash or number and the transactions index position.',
                args: [{
                    name: 'hashStringOrBlockNumber',
                    type: 'String|Number',
                    desc: 'The block number or hash. Or the string "earliest", "latest" or "pending"'
                }, {
                    name: 'indexNumber',
                    type: 'Number',
                    desc: 'The transactions index position.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getTransactionReceipt: {
                desc: 'Returns the receipt of a transaction by transaction hash.',
                args: [{
                    name: 'hashString',
                    type: 'String',
                    desc: 'The transaction hash.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            getTransactionCount: {
                desc: 'Get the numbers of transactions sent from this address.',
                args: [{
                    name: 'addressHexString',
                    type: 'String',
                    desc: 'The address to get the numbers of transactions from.'
                }, {
                    name: 'defaultBlock',
                    type: 'String|Number',
                    desc: 'If you pass this parameter it will not use the default block set with web3.eth.defaultBlock.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            sendTransaction: {
                desc: 'Sends a transaction to the network.',
                args: [{
                    name: 'transactionObject',
                    type: 'Object',
                    desc: 'The transaction object to send: {from[, to][, value][, gas][, gasPrice][, data][, nonce]}'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            sendRawTransaction: {
                desc: 'Sends an already signed transaction.',
                args: [{
                    name: 'signedTransactionData',
                    type: 'String',
                    desc: 'Signed transaction data in HEX format'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            sign: {
                desc: 'Signs data from a specific account. This account needs to be unlocked.',
                args: [{
                    name: 'address',
                    type: 'String',
                    desc: 'Address to sign with.'
                }, {
                    name: 'dataToSign',
                    type: 'String',
                    desc: 'Data to sign.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            call: {
                desc: 'Executes a message call transaction, which is directly executed in the VM of the node, but never mined into the blockchain.',
                args: [{
                    name: 'callObject',
                    type: 'String',
                    desc: 'Address to sign with.'
                }, {
                    name: 'defaultBlock',
                    type: 'String',
                    optional: true,
                    desc: 'Data to sign.'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            estimateGas: {
                desc: 'Executes a message call or transaction, which is directly executed in the VM of the node, but never mined into the blockchain and returns the amount of the gas used.',
                args: [{
                    name: 'callObject',
                    type: 'Object',
                    desc: 'The transaction object to send: {[from][, to][, value][, gas][, gasPrice][, data][, nonce]}'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            filter: {
                // TODO: add description
                desc: '',
                args: [{
                    name: 'options',
                    type: 'String|Object',
                    desc: 'The string "latest" or "pending" to watch for changes in the latest block or pending transactions respectively. Or a filter options object as follows: {fromBlock: Number|String, toBlock: Number|String, address: String, topics: StringArray}'
                }, {
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'Watch callback'
                }]
            },
            // TODO filters
            // watch                           : ['callback'],
            // stopWatching                    : ['callback'],
            contract: {
                desc: 'Creates a contract object for a solidity contract, which can be used to initiate contracts on an address.',
                args: [{
                    name: 'abiArray',
                    type: 'Array',
                    desc: 'ABI array with descriptions of functions and events of the contract.'
                }]
            },
            getCompilers: {
                desc: 'Gets a list of available compilers.',
                args: [{
                    name: 'callback',
                    type: 'Function',
                    optional: true,
                    desc: 'If you pass a callback the HTTP request is made asynchronous.'
                }]
            },
            compile: { // TODO we should auto hide these depending on output from getCompilers
                lll: {
                    desc: 'Compiles LLL source code.',
                    args: [{
                        name: 'sourceString',
                        type: 'String',
                        desc: 'The LLL source code.'
                    }, {
                        name: 'callback',
                        type: 'Function',
                        optional: true,
                        desc: 'Watch callback'
                    }]
                },
                solidity: {
                    desc: 'Compiles solidity source code',
                    args: [{
                        name: 'sourceString',
                        type: 'String',
                        desc: 'The solidity source code.'
                    }, {
                        name: 'callback',
                        type: 'Function',
                        optional: true,
                        desc: 'Watch callback'
                    }],
                },
                serpent: {
                    desc: 'Compiles serpent source code',
                    args: [{
                        name: 'sourceString',
                        type: 'String',
                        desc: 'The serpent source code.'
                    }, {
                        name: 'callback',
                        type: 'Function',
                        optional: true,
                        desc: 'Watch callback'
                    }]
                }
            },
            namereg: {
                desc: 'Returns GlobalRegistrar object.'
            }
        },

        db: {
            putString: {
                desc: 'Store a string in the local leveldb database.',
                args: [{
                    name: 'db',
                    type: 'String',
                    desc: 'The database to store to.'
                }, {
                    name: 'key',
                    type: 'String',
                    desc: 'The name of the store.'
                }, {
                    name: 'value',
                    type: 'String',
                    desc: 'The string value to store.'
                }]
            },
            getString: {
                desc: 'Retrieve a string from the local leveldb database. (db, key)',
                args: [{
                    name: 'db',
                    type: 'String',
                    desc: 'The database string name to retrieve from.'
                }, {
                    name: 'key',
                    type: 'String',
                    desc: 'The name of the store.'
                }]
            },
            putHex: {
                desc: 'Store binary data in the local leveldb database. (db, key, value)',
                args: [{
                    name: 'db',
                    type: 'String',
                    desc: 'The database to store to.'
                }, {
                    name: 'key',
                    type: 'String',
                    desc: 'The name of the store.'
                }, {
                    name: 'value',
                    type: 'String',
                    desc: 'The HEX string to store.'
                }]
            },
            getHex: {
                desc: 'Retrieve binary data from the local leveldb database. (db, key)',
                args: [{
                    name: 'db',
                    type: 'String',
                    desc: 'The database string name to retrieve from.'
                }, {
                    name: 'key',
                    type: 'String',
                    desc: 'The name of the store.'
                }]
            }
        }
    }
};

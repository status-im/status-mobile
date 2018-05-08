## 1 to 1 messages


The ssh_post message on 1 to 1 chats looks like:
```
{"jsonrpc":"2.0","id":3146,"method":"shh_post","params":[{"sig":"0x046f29b16371c05880c3da0279c7b8f958f97be79e00ca2a8f9e951eb254ccf8ead793af564a66c952668ad9e6e8f6dac5773bd1087d87273ebcece8da03420085","symKeyID":"7aee3646de46c5b175e5ff7cc16ebe1c4488844700f241fb36a2bc0829cea25d","payload":"0x5b227e236334222c5b2270757475222c22746578742f706c61696e222c227e3a757365722d6d657373616765222c3135323336313837323338323330312c313532333631383732333832315d5d","topic":"0xd977be6d","ttl":10,"powTarget":0.001,"powTime":1}]}
```

And its decrypted message is:
```
["~#c4",["hello world","text/plain","~:user-message",152361872382301,1523618723821]]
```

Basically the only difference between 1to1 and public is the field message type which in this case is set to `~:user-message`.s



### Ability to send 1 to 1 conversation



### Ability to subscribe to 1 to 1 conversation
### Ability to unsubscribe from a 1 to 1 conversation
### Documented API for 1 to 1 conversations
### Working examples for 1 to 1 conversations

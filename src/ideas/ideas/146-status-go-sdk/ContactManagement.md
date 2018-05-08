## Contact management

### Invite new contact
The UI for Inviting a new contact should be something like:
```
if account, err := conn.Invite("my_friend_public_key"); err != nil {
  panic("An error occurred while inviting a friend")
}
```

Before starting to chat with someone privately you should have its contact details. In order to do that, the next process should happen

##### Generate a new symkey (shh_newSymKey):
Generates a new symetric key
```
{"jsonrpc":"2.0","id":872,"method":"shh_newSymKey","params":[]}
```


##### GetSymKey (shh_getSymKey):
Polls any message for the symetric key
```
{"jsonrpc":"2.0","id":873,"method":"shh_getSymKey","params":["14829092e1b30cb9ab643ef9aa3c37e5a576e69821259cc690f8cccedd08dc94"]}
```
** [ TBD : How do i calculate what's on params? ] **


##### Create filter for new topic (shh_newMessageFilter):
```
{"jsonrpc":"2.0","id":879,"method":"shh_newMessageFilter","params":[{"topics":["0x6c0b63af"],"symKeyID":"14829092e1b30cb9ab643ef9aa3c37e5a576e69821259cc690f8cccedd08dc94","allowP2P":true}]}
```
Main difference here with public channels is how the topic is calculated
**[ TODO DEFINE HOW TOPIC IS CALCULATED ]**


##### Send first message with my contact information (shh_post)
```
{"jsonrpc":"2.0","id":880,"method":"shh_post","params":[{"sig":"0x046f29b16371c05880c3da0279c7b8f958f97be79e00ca2a8f9e951eb254ccf8ead793af564a66c952668ad9e6e8f6dac5773bd1087d87273ebcece8da03420085","pubKey":"<my_friend_public_key>","payload":"<payload>...
```
Where the decrypted payload looks like :
```
["~#c1",["0xcfbb3e7c45ced993287039e9a3aa37d15d6c2f80ba2599e07ee53520f239d4d1","0x6c0b63af",["~#c2",["<username>","data:image/png;base64,......<image binary>","b5b16e93dbd4bbcb2448b98bfbb9e0062ea18603",null]]]]
```


### Accept new contact

**[ TBD ]**

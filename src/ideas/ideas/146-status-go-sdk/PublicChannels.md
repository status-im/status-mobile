## Public channels

Public channels interaction is about how the developer will be interacting with public channels.

### Ability to join public channels

A new public channel will be joined using Join method:
```
ch, err := conn.Join("my_channel")
if err != nil {
  panic("Couldn't join my_channel")
}
```

### Ability to publish messages on public channels
You can use the Channel object to publish messages on it as follows:
```
ch, _ := conn.Join("my_channel")
ch.Publish("Hello world")
```

### Ability to subscribe to public channels
Subscribing a channel means a script will be able to listen to any messages on a specific public channel. This can be achieved with Channel Subscribe method as follows:
```
ch, _ := conn.Join("my_channel")
ch.Subscribe(func(m *sdk.Msg) {
  log.Println("Message from ", m.From, " with body: ", m.Text)
}
```

### Ability to unsubscribe from a public channel
In order to unsubscribe from a specific public channel you just have to call Unsubscribe method.
```
ch, _ := conn.Join("my_channel")
ch.Subscribe(func(m *sdk.Msg) {
  if m.Text == "BYE!" {
    ch.Unsubscribe()
  }
}
```


### Documented API for public channels interaction

This document can be adapted as a documentation for public channels interaction


### Working examples for public channel interaction

Actually [here](https://github.com/status-im/status-go/blob/sdk/sdk/examples/) you'll find  an example of a "ping pong game".

## Status messaging basic interaction

** * TODO its still not clear how username works on the new protocol, it may change the methods using it, as it's not used atm. **

###  Setup a new connection

SDK should offer an **easy** but **configurable** way to setup the status node.


#### Connect

Connect method will create a new connection, attach the default configuration, and login with the provided password.

```
conn, err := sdk.Connect(username, password)
```


###  Ability to close a specific connection

In order to close all channels, sdk.Conn object should offer a `Close` method.
```
conn, err := sdk.Connect(username, password)
defer conn.Close()
```

###  Ability to change connection configuration

Even `Connect` method is suitable for almost all situations, some developers may want to customize statusNode configuration. To accomplish that, one can simply divide Connect method on its internal calls like:
```
cfg := sdk.DefaultConfig()
cfg.NodeConfig.WhisperConfig.MinimumPoW = 0.001

conn := sdk.New()
if err := conn.Start(cfg); err != nil {
  panic("Couldn't connect to status")
}

if err := conn.SignupOrLogin("username", "password"); err != nil {
  panic(err)
}
```

### Ability to sign up on the platform

`SignupOrLogin` method is provided so you can sign up as a specific user

```
if err := conn.SignupOrLogin("username", "password"); err != nil {
  panic(err)
}
```


###  Ability to login as a specific account

`Login` method provides an interface to log in

```
if err := conn.Login("username", "password"); err != nil {
  panic(err)
}
```


### Documented API for basic sdk interaction

This document can be adapted as a documentation for basic sdk interaction

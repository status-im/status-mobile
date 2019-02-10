(ns status-im.chat.commands.protocol)

(def or-scopes
  "Scope contexts representing OR choices"
  [#{:personal-chats :group-chats :public-chats}])

(defprotocol Command
  "Protocol for defining command message behaviour"
  (id [this] "Identifier of the command, used to look-up command display name as well")
  (scope [this]
    "Scope of the command, defined as set of values representing contexts
     where in which command is available, together with `id` it forms unique
     identifier for each command.
     Available values for the set are:
     `id-of-the-any-chat` - command if available only for the specified chat
     `:personal-chats` - command is available for any personal 1-1 chat
     `:group-chats` - command is available for any group chat
     `:public-chats` - command is available for any public chat ")
  (description [this] "Description of the command")
  (parameters [this]
    "Ordered sequence of command parameter templates, where each parameter
     is defined as map consisting of mandatory `:id`, `:title` and `:type` keys,
     and optional `:suggestions` field.
     When used, `:suggestions` contains reference to any generic helper component
     rendering suggestions for the argument (input code will handle when and where
     to render it)")
  (validate [this parameters cofx]
    "Function validating the parameters once command is send. Takes parameters map
    and `cofx` map as argument, returns either `nil` meaning that no errors were
    found and command send workflow can proceed, or one/more errors to display.
    Each error is represented by the map containing `:title` and `:description` keys.")
  (on-send [this command-message cofx]
    "Function which can provide any extra effects to be produced in addition to
    normal message effects which happen whenever message is sent")
  (on-receive [this command-message cofx]
    "Function which can provide any extra effects to be produced in addition to
    normal message effects which happen when particular command message is received")
  (short-preview [this command-message]
    "Function rendering the short-preview of the command message, used when
    displaying the last message in list of chats on home tab.
    There is no argument names `parameters` anymore, as the message object
    contains everything needed for short-preview/preview to render.")
  (preview [this command-message]
    "Function rendering preview of the command message in message stream"))

(defprotocol Yielding
  "Protocol for defining commands yielding control back to application during the send flow"
  (yield-control [this parameters cofx]
    "Function, which steps out of the normal command workflow (`validate-and-send`)
    and yields control back to application before sending.
    Useful for cases where we want to use command input handling (parameters) and/or
    validating, but we don't want to send message before yielding control elsewhere."))

(defprotocol EnhancedParameters
  "Protocol for command messages which wish to modify/inject additional data into parameters,
  other then those collected from the chat input.
  Good example would be the `/send` and `/request` commands - we would like to indicate
  network selected in sender's device, but we of course don't want to force user to type
  it in when it could be effortlessly looked up from context.
  Another usage would be for example command where one of the input parameters will be
  hashed after validation and we would want to avoid the original unhashed parameter
  to be ever saved on the sender device, nor to be sent over the wire.
  For maximal flexibility, parameters can be enhanced both on the sending side and receiving
  side, as sometimes thing needs to be added/enhanced in parameters map which are depending
  on the receiver context - as for example calculated fiat price values for the `/request`
  command"
  (enhance-send-parameters [this parameters cofx]
    "Function which takes original parameters + cofx map and returns new map of parameters")
  (enhance-receive-parameters [this parameters cofx]
    "Function which takes original parameters + cofx map and returns new map of parameters"))

(defprotocol Extension
  "Protocol for defining extension"
  (extension-id [this]))
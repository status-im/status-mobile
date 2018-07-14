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
     `:public-chats` - command is available for any public chat
     `:requested` - command is available only when there is an outstanding request")
  (parameters [this]
    "Ordered sequence of command parameter templates, where each parameter 
     is defined as map consisting of mandatory `:id`, `:title` and `:type` keys, 
     and optional `:suggestions` field.
     When used, `:suggestions` containes reference to any generic helper component
     rendering suggestions for the argument (input code will handle when and where
     to render it)")
  (validate [this parameters cofx]
    "Function validating the parameters once command is send. Takes parameters map
    and `cofx` map as argument, returns either `nil` meaning that no errors were
    found and command send workflow can proceed, or sequence of errors to display")
  (yield-control [this parameters cofx]
    "Optional function, which if implemented, can step out of the normal command
    workflow (`validate-and-send`) and yield control back to application before sending.
    Useful for cases where we want to use command input handling (parameters) and/or
    validating, but we don't want to send message before yielding control elsewhere.")
  (on-send [this message-id parameters cofx]
    "Function which can provide any extra effects to be produced in addition to 
    normal message effects which happen whenever message is sent")
  (on-receive [this command-message cofx]
    "Function which can provide any extre effects to be produced in addition to
    normal message effects which happen when particular command message is received")
  (short-preview [this command-message cofx]
    "Function rendering the short-preview of the command message, used when
    displaying the last message in list of chats on home tab.
    There is no argument names `parameters` anymore, as the message object
    contains everything needed for short-preview/preview to render.")
  (preview [this command-message cofx]
    "Function rendering preview of the command message in message stream"))

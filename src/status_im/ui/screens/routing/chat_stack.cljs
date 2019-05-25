(ns status-im.ui.screens.routing.chat-stack)

(def chat-stack
  {:name    :chat-stack
   :screens [:home
             :chat
             :profile
             :new
             :new-chat
             :qr-scanner
             :take-picture
             :extension-screen-holder
             :new-group
             :add-participants-toggle-list
             :contact-toggle-list
             :group-chat-profile
             :new-public-chat
             :stickers
             :stickers-pack]
   :config  {:initialRouteName :home}})

(ns status-im.ui.screens.routing.chat-stack)

(def chat-stack
  {:name    :chat-stack
   :screens [:home
             :chat
             :select-chat
             :profile
             :take-picture
             :new-group
             :add-participants-toggle-list
             :contact-toggle-list
             :group-chat-profile
             :stickers
             :stickers-pack]
   :config  {:initialRouteName :home
             :emptyRightPaneName :select-chat}})

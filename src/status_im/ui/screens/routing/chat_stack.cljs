(ns status-im.ui.screens.routing.chat-stack
  (:require [status-im.utils.config :as config]))

(def chat-stack
  {:name    :chat-stack
   :screens (cond-> [:home
                     :chat
                     :profile
                     :new
                     :new-chat
                     :qr-scanner
                     :take-picture
                     :new-group
                     :add-participants-toggle-list
                     :contact-toggle-list
                     :group-chat-profile
                     :new-public-chat
                     :open-dapp
                     :dapp-description
                     :browser
                     :stickers
                     :stickers-pack]
              config/hardwallet-enabled?
              (concat [:hardwallet-connect :enter-pin]))
   :config  {:initialRouteName :home}})

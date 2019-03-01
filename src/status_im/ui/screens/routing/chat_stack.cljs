(ns status-im.ui.screens.routing.chat-stack
  (:require [status-im.utils.config :as config]))

(def chat-stack
  {:name       :chat-stack
   :screens    [{:name    :chat-main-stack
                 :screens (cond->
                           [:home
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
                 :config  {:initialRouteName :home}}
                :chat-modal
                :show-extension-modal
                :stickers-pack-modal
                {:name    :wallet-send-modal-stack
                 :screens [:wallet-send-transaction-modal
                           :wallet-transaction-sent-modal
                           :wallet-transaction-fee]
                 :config  {:initialRouteName :wallet-send-transaction-modal}}
                {:name    :wallet-send-modal-stack-with-onboarding
                 :screens [:wallet-onboarding-setup-modal
                           :wallet-send-transaction-modal
                           :wallet-transaction-sent-modal
                           :wallet-transaction-fee]
                 :config  {:initialRouteName :wallet-onboarding-setup-modal}}
                :wallet-sign-message-modal]
   :config     {:mode             :modal
                :initialRouteName :chat-main-stack}})

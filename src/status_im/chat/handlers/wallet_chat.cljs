(ns status-im.chat.handlers.wallet-chat
  (:require [re-frame.core :refer [after enrich path dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.constants :refer [wallet-chat-id]]
            [clojure.string :as s]))

(def dapp-contact
  {:whisper-identity wallet-chat-id
   :name             (s/capitalize wallet-chat-id)
   :dapp?            true})


(register-handler :init-wallet-chat
  (u/side-effect!
    (fn [{:keys [chats]}]
      (when-not (chats wallet-chat-id)
        (dispatch [:add-chat
                   wallet-chat-id
                   {:name     "Wallet"
                    :dapp-url "http://192.168.1.125:3450"}])
        (dispatch [:add-contacts [dapp-contact]])))))


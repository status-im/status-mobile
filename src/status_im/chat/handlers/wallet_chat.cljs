(ns status-im.chat.handlers.wallet-chat
  (:require [re-frame.core :refer [after enrich path dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.constants :refer [wallet-chat-id]]))

(register-handler :init-wallet-chat
  (u/side-effect!
    (fn []
      (dispatch [:add-chat wallet-chat-id {:name     "Wallet"
                                           :dapp-url "http://127.0.0.1:3450"}]))))


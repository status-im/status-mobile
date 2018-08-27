(ns status-im.ui.screens.add-new.new-chat.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.contact-code.model :as models.contact-code]
            [status-im.ui.screens.add-new.new-chat.db :as db]
            [status-im.utils.ethereum.stateofus :as stateofus]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-fx
 :resolve-whisper-identity
 (fn [{:keys [web3 registry ens-name cb]}]
   (stateofus/pubkey web3 registry ens-name cb)))

(defn handle-response [err id]
  (when id
    (re-frame/dispatch [:new-chat/set-new-identity id])))

(handlers/register-handler-fx
 :new-chat/set-new-identity
 (fn [cofx [_ new-identity]]
   (models.contact-code/validate
    new-identity
    handle-response
    cofx)))

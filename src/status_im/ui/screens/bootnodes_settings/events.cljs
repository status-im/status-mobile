(ns status-im.ui.screens.bootnodes-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.i18n :as i18n]
            [status-im.transport.core :as transport]
            status-im.ui.screens.bootnodes-settings.edit-bootnode.events
            [status-im.utils.ethereum.core :as ethereum]))

(defn toggle-custom-bootnodes [value {:keys [db] :as cofx}]
  (let [network  (get-in db [:account/account :network])
        settings (get-in db [:account/account :settings])]
    (handlers-macro/merge-fx cofx
                             (accounts-events/update-settings
                              (assoc-in settings [:bootnodes network] value)
                              [:logout]))))

(handlers/register-handler-fx
 :toggle-custom-bootnodes
 (fn [cofx [_ value]]
   (toggle-custom-bootnodes value cofx)))

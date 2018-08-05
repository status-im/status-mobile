(ns status-im.ui.screens.bootnodes-settings.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.models :as accounts.models]
            status-im.ui.screens.bootnodes-settings.edit-bootnode.events))

(defn toggle-custom-bootnodes [value {:keys [db] :as cofx}]
  (let [network  (get-in db [:account/account :network])
        settings (get-in db [:account/account :settings])]
    (handlers-macro/merge-fx cofx
                             (accounts.models/update-settings
                              (assoc-in settings [:bootnodes network] value)
                              [:logout]))))

(handlers/register-handler-fx
 :toggle-custom-bootnodes
 (fn [cofx [_ value]]
   (toggle-custom-bootnodes value cofx)))

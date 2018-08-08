(ns status-im.ui.screens.currency-settings.events
  (:require [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.wallet.events :as wallet.events]))

(handlers/register-handler-fx
 :wallet.settings/set-currency
 (fn [{:keys [db] :as cofx} [_ currency]]
   (let [settings     (get-in db [:account/account :settings])
         new-settings (assoc-in settings [:wallet :currency] currency)]
     (handlers-macro/merge-fx cofx
                              (accounts.models/update-settings new-settings)
                              (wallet.events/update-wallet)))))

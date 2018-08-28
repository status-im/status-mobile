(ns status-im.ui.screens.currency-settings.models
  (:require [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.models.wallet :as wallet]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn set-currency [currency {:keys [db] :as cofx}]
  (let [settings     (get-in db [:account/account :settings])
        new-settings (assoc-in settings [:wallet :currency] currency)]
    (handlers-macro/merge-fx cofx
                             (accounts.models/update-settings new-settings)
                             (wallet/update-wallet))))

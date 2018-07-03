(ns status-im.ui.screens.currency-settings.subs
  (:require [re-frame.core :as re-frame]))

(defn get-user-currency [db]
  (get-in db [:account/account :settings :wallet :currency] :usd))

(re-frame/reg-sub
 :wallet.settings/currency
 (fn [db]
   (get-user-currency db)))

(ns status-im.ui.screens.currency-settings.subs
  (:require [re-frame.core :as re-frame]))

;;TODO(goranjovic): this fn should go to `status-im.ui.screens.currency-settings.subs`
;; but it can't because of cyclic dependencies
(defn get-currency [db]
  (or (get-in db [:account/account :settings :wallet :currency]) :usd))

(re-frame/reg-sub
 :wallet.settings/currency
 (fn [db]
   (get-currency db)))

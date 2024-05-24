(ns status-im.contexts.wallet.swap.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx :wallet/start-swap
 (fn [{:keys [_db]}]
   {:fx [[:dispatch
          [:toasts/upsert
           {:id   :swap-error
            :type :negative
            :text "Swap is under construction"}]]]}))

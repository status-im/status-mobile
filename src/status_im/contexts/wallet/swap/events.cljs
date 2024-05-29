(ns status-im.contexts.wallet.swap.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx :wallet.swap/start
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:open-modal :screen/wallet.swap-select-asset-to-pay]]]}))

(rf/reg-event-fx :wallet.swap/select-asset-to-pay
 (fn [{:keys [db]} token]
   {:db (assoc-in db [:wallet :ui :swap :asset-to-pay] token)
    :fx [[:dispatch
          [:toasts/upsert
           {:id   :swap-error
            :type :negative
            :text "Not implemented yet"}]]]}))

(rf/reg-event-fx :wallet.swap/clean-asset-to-pay
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :swap] dissoc :asset-to-pay)}))

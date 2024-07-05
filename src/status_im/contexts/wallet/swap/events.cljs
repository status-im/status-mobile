(ns status-im.contexts.wallet.swap.events
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]))

(rf/reg-event-fx :wallet.swap/start
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:open-modal :screen/wallet.swap-select-asset-to-pay]]]}))

(rf/reg-event-fx :wallet.swap/select-asset-to-pay
 (fn [{:keys [db]} [{:keys [token network]}]]
   {:db (-> db
            (assoc-in [:wallet :ui :swap :asset-to-pay] token)
            (assoc-in [:wallet :ui :swap :network] network))
    :fx [(if network
           [:dispatch
            [:toasts/upsert
             {:id   :swap-error
              :type :negative
              :text "Not implemented yet"}]]
           [:dispatch
            [:show-bottom-sheet
             {:content (fn []
                         [network-selection/view
                          {:token-symbol      (:symbol token)
                           :on-select-network (fn [network]
                                                (rf/dispatch [:hide-bottom-sheet])
                                                (rf/dispatch
                                                 [:wallet.swap/select-asset-to-pay
                                                  {:token token
                                                   :network network
                                                   :stack-id
                                                   :screen/wallet.swap-select-asset-to-pay}]))}])}]])]}))

(rf/reg-event-fx :wallet.swap/clean-asset-to-pay
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :swap] dissoc :asset-to-pay)}))

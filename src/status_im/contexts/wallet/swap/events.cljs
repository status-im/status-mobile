(ns status-im.contexts.wallet.swap.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]
            [utils.number]))

(rf/reg-event-fx :wallet.swap/start
 (fn [{:keys [_db]}]
   {:fx [[:dispatch [:open-modal :screen/wallet.swap-select-asset-to-pay]]]}))

(rf/reg-event-fx :wallet.swap/select-asset-to-pay
 (fn [{:keys [db]} [{:keys [token network]}]]
   {:db (-> db
            (assoc-in [:wallet :ui :swap :asset-to-pay] token)
            (assoc-in [:wallet :ui :swap :network] network))
    :fx (if network
          [[:dispatch [:navigate-to :screen/wallet.swap-propasal]]
           [:dispatch [:wallet.swap/set-default-slippage]]]
          [[:dispatch
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
                                                   :screen/wallet.swap-select-asset-to-pay}]))}])}]]])}))

(rf/reg-event-fx :wallet.swap/clean-asset-to-pay
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :swap] dissoc :asset-to-pay)}))

(rf/reg-event-fx :wallet.swap/set-default-slippage
 (fn [{:keys [db]}]
   {:db
    (assoc-in db [:wallet :ui :swap :max-slippage] constants/default-slippage)}))

(rf/reg-event-fx :wallet.swap/set-max-slippage
 (fn [{:keys [db]} [max-slippage]]
   {:db (assoc-in db [:wallet :ui :swap :max-slippage] (utils.number/parse-float max-slippage))}))

(rf/reg-event-fx :wallet.swap/select-asset-to-receive
 (fn [{:keys [db]} [{:keys [token]}]]
   {:db (assoc-in db [:wallet :ui :swap :asset-to-receive] token)}))

(rf/reg-event-fx :wallet.swap/set-pay-amount
 (fn [{:keys [db]} [amount]]
   {:db (assoc-in db [:wallet :ui :swap :pay-amount] amount)}))

(rf/reg-event-fx :wallet.swap/set-swap-proposal
 (fn [{:keys [db]} [swap-proposal]]
   {:db (assoc-in db [:wallet :ui :swap :swap-proposal] swap-proposal)}))

(rf/reg-event-fx :wallet.swap/set-provider
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :swap :providers] [constants/swap-default-provider])}))

(rf/reg-event-fx :wallet.swap/recalculate-fees
 (fn [{:keys [db]} [loading-fees?]]
   {:db (assoc-in db [:wallet :ui :swap :loading-fees?] loading-fees?)}))

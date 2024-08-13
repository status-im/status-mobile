(ns status-im.contexts.wallet.swap.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]
            [taoensso.timbre :as log]
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
          [[:dispatch
            [:navigate-to-within-stack
             [:screen/wallet.setup-swap :screen/wallet.swap-select-asset-to-pay]]]
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

(rf/reg-event-fx :wallet.swap/recalculate-fees
 (fn [{:keys [db]} [loading-fees?]]
   {:db (assoc-in db [:wallet :ui :swap :loading-fees?] loading-fees?)}))

(rf/reg-event-fx :wallet/start-get-swap-proposal
 (fn [{:keys [db]} [{:keys [amount-in amount-out]}]]
   (let [wallet-address          (get-in db [:wallet :current-viewing-account-address])
         {:keys [asset-to-pay asset-to-receive
                 network]}       (get-in db [:wallet :ui :swap])
         test-networks-enabled?  (get-in db [:profile/profile :test-networks-enabled?])
         networks                ((if test-networks-enabled? :test :prod)
                                  (get-in db [:wallet :networks]))
         network-chain-ids       (map :chain-id networks)
         pay-token-decimal       (:decimals asset-to-pay)
         pay-token-id            (:symbol asset-to-pay)
         receive-token-id        (:symbol asset-to-receive)
         receive-token-decimals  (:decimals asset-to-receive)
         gas-rates               constants/gas-rate-medium
         amount-in-hex           (if amount-in
                                   (send-utils/amount-in-hex amount-in pay-token-decimal)
                                   0)
         amount-out-hex          (when amount-out
                                   (send-utils/amount-in-hex amount-out receive-token-decimals))
         to-address              wallet-address
         from-address            wallet-address
         swap-chain-id           (:chain-id network)
         disabled-to-chain-ids   (filter #(not= % swap-chain-id) network-chain-ids)
         disabled-from-chain-ids (filter #(not= % swap-chain-id) network-chain-ids)
         from-locked-amount      {}
         send-type               constants/send-type-swap
         request-uuid            (str (random-uuid))
         params                  [(cond->
                                    {:uuid                 request-uuid
                                     :sendType             send-type
                                     :addrFrom             from-address
                                     :addrTo               to-address
                                     :tokenID              pay-token-id
                                     :toTokenID            receive-token-id
                                     :disabledFromChainIDs disabled-from-chain-ids
                                     :disabledToChainIDs   disabled-to-chain-ids
                                     :gasFeeMode           gas-rates
                                     :fromLockedAmount     from-locked-amount}
                                    amount-in  (assoc :amountIn amount-in-hex)
                                    amount-out (assoc :amountOut amount-out-hex))]]
     (when-let [amount (or amount-in amount-out)]
       {:db            (update-in db
                                  [:wallet :ui :swap]
                                  #(-> %
                                       (assoc
                                        :last-request-uuid      request-uuid
                                        :amount                 amount
                                        :loading-swap-proposal? true)
                                       (dissoc :error-response)))
        :json-rpc/call [{:method   "wallet_getSuggestedRoutesV2Async"
                         :params   params
                         :on-error (fn [error]
                                     (rf/dispatch [:wallet/swap-proposal-error error])
                                     (log/error "failed to get suggested routes (async)"
                                                {:event  :wallet/start-get-swap-proposal
                                                 :error  (:message error)
                                                 :params params}))}]}))))

(rf/reg-event-fx :wallet/swap-proposal-success
 (fn [{:keys [db]} [swap-proposal]]
   (let [last-request-uuid (get-in db [:wallet :ui :swap :last-request-uuid])
         request-uuid      (:uuid swap-proposal)
         best-routes       (:best swap-proposal)
         error-response    (:error-response swap-proposal)]
     (when (= request-uuid last-request-uuid)
       {:db (update-in db
                       [:wallet :ui :swap]
                       assoc
                       :swap-proposal          (first best-routes)
                       :error-response         (when (empty? best-routes) error-response)
                       :loading-swap-proposal? false)}))))

(rf/reg-event-fx :wallet/swap-proposal-error
 (fn [{:keys [db]} [error-message]]
   {:db (-> db
            (update-in [:wallet :ui :swap] dissoc :route :swap-proposal)
            (assoc-in [:wallet :ui :swap :loading-swap-proposal?] false)
            (assoc-in [:wallet :ui :swap :error-response] error-message))
    :fx [[:dispatch
          [:toasts/upsert
           {:id   :swap-proposal-error
            :type :negative
            :text error-message}]]]}))

(rf/reg-event-fx :wallet/stop-get-swap-proposal
 (fn []
   {:json-rpc/call [{:method   "wallet_stopSuggestedRoutesV2AsyncCalcualtion"
                     :params   []
                     :on-error (fn [error]
                                 (log/error "failed to stop fetching swap proposals"
                                            {:event :wallet/stop-get-swap-proposal
                                             :error error}))}]}))

(rf/reg-event-fx :wallet/clean-swap-proposal
 (fn [{:keys [db]}]
   {:db (update-in db
                   [:wallet :ui :swap]
                   dissoc
                   :last-request-uuid
                   :swap-proposal
                   :error-response
                   :loading-swap-proposal?)}))

(rf/reg-event-fx :wallet/clean-swap
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :swap)}))

(ns status-im.contexts.wallet.swap.events
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]
            [taoensso.timbre :as log]
            [utils.address :as address]
            [utils.debounce :as debounce]
            [utils.hex :as hex]
            [utils.i18n :as i18n]
            [utils.number]
            [utils.number :as number]))

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
                   :loading-swap-proposal?
                   :approval-transaction-id)}))

(rf/reg-event-fx :wallet/clean-swap
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :swap)}))

(rf/reg-event-fx :wallet/swap-transaction
 (fn [{:keys [db]} [sha3-pwd]]
   (let [wallet-address         (get-in db
                                        [:wallet
                                         :current-viewing-account-address])
         {:keys [asset-to-pay asset-to-receive
                 swap-proposal network amount
                 approval-transaction-id
                 max-slippage]} (get-in db [:wallet :ui :swap])
         transactions           (get-in db [:wallet :transactions])
         approval-transaction   (when approval-transaction-id
                                  (get transactions approval-transaction-id))
         already-approved?      (and approval-transaction
                                     (= (:status approval-transaction)
                                        :confirmed))
         approval-required?     (and (:approval-required swap-proposal)
                                     (not already-approved?))
         multi-transaction-type constants/multi-transaction-type-swap
         swap-chain-id          (:chain-id network)
         token-id-from          (:symbol asset-to-pay)
         token-id-to            (:symbol asset-to-receive)
         erc20-transfer?        (and asset-to-pay (not= token-id-from "ETH"))
         eth-transfer?          (and asset-to-pay (not erc20-transfer?))
         token-address          (when erc20-transfer?
                                  (get-in asset-to-pay
                                          [:balances-per-chain swap-chain-id
                                           :address]))
         data                   (when erc20-transfer?
                                  (native-module/encode-transfer
                                   (address/normalized-hex wallet-address)
                                   (:amount-in swap-proposal)))
         transaction-paths      (if approval-required?
                                  [(utils/approval-path
                                    {:route         swap-proposal
                                     :token-address token-address
                                     :from-address  wallet-address
                                     :to-address    wallet-address})]
                                  [(utils/transaction-path
                                    {:to-address          wallet-address
                                     :from-address        wallet-address
                                     :route               swap-proposal
                                     :token-address       token-address
                                     :token-id-from       token-id-from
                                     :token-id-to         token-id-to
                                     :data                data
                                     :slippage-percentage max-slippage
                                     :eth-transfer?       eth-transfer?})])
         request-params         [(utils/multi-transaction-command
                                  {:from-address wallet-address
                                   :to-address wallet-address
                                   :from-asset token-id-from
                                   :to-asset (if approval-required?
                                               token-id-from
                                               token-id-to)
                                   :amount-out (if eth-transfer?
                                                 (:amount-out swap-proposal)
                                                 "0x0")
                                   :multi-transaction-type
                                   multi-transaction-type})
                                 transaction-paths
                                 sha3-pwd]]
     (log/info "multi transaction called")
     {:json-rpc/call [{:method     "wallet_createMultiTransaction"
                       :params     request-params
                       :on-success (fn [result]
                                     (when result
                                       (let [receive-token-decimals (:decimals asset-to-receive)
                                             amount-out (:amount-out swap-proposal)
                                             decimals-to-display
                                             (min
                                              receive-token-decimals
                                              constants/min-token-decimals-to-display)
                                             receive-amount (when amount-out
                                                              (number/remove-trailing-zeroes
                                                               (.toFixed (number/convert-to-whole-number
                                                                          (native-module/hex-to-number
                                                                           (hex/normalize-hex
                                                                            amount-out))
                                                                          receive-token-decimals)
                                                                         decimals-to-display)))]
                                         (rf/dispatch [:wallet.swap/add-authorized-transaction
                                                       (cond-> {:transaction result
                                                                :approval-transaction?
                                                                approval-required?}
                                                         (not approval-required?)
                                                         (assoc :swap-data
                                                                {:pay-token-symbol token-id-from
                                                                 :pay-amount amount
                                                                 :receive-token-symbol token-id-to
                                                                 :receive-amount receive-amount}))])
                                         (rf/dispatch [:hide-bottom-sheet])
                                         (rf/dispatch [:dismiss-modal
                                                       (if approval-required?
                                                         :screen/wallet.swap-set-spending-cap
                                                         :screen/wallet.swap-confirmation)])
                                         (when-not approval-required?
                                           (rf/dispatch [:wallet/select-account-tab :activity])
                                           (debounce/debounce-and-dispatch [:wallet/clean-swap] 1000)
                                           (debounce/debounce-and-dispatch
                                            [:toasts/upsert
                                             {:id   :swap-transaction-pending
                                              :icon :i/info
                                              :type :neutral
                                              :text (i18n/label :t/swapping-to
                                                                {:pay-amount           amount
                                                                 :pay-token-symbol     token-id-from
                                                                 :receive-token-symbol token-id-to
                                                                 :receive-amount       receive-amount})}]
                                            500)))))
                       :on-error   (fn [error]
                                     (log/error "failed swap transaction"
                                                {:event  :wallet/swap-transaction
                                                 :error  error
                                                 :params request-params})
                                     (rf/dispatch [:toasts/upsert
                                                   {:id   :swap-transaction-error
                                                    :type :negative
                                                    :text (:message error)}]))}]})))

(rf/reg-event-fx :wallet.swap/add-authorized-transaction
 (fn [{:keys [db]} [{:keys [transaction swap-data approval-transaction?]}]]
   (let [transactions         (get-in db [:wallet :transactions] {})
         transaction-batch-id (:id transaction)
         transaction-hashes   (:hashes transaction)
         transaction-ids      (flatten (vals transaction-hashes))
         transaction-id       (first transaction-ids)
         transaction-details  (cond-> (send-utils/map-multitransaction-by-ids transaction-batch-id
                                                                              transaction-hashes)
                                :always   (assoc-in [transaction-id :tx-type] :swap)
                                swap-data (assoc-in [transaction-id :swap-data] swap-data))
         swap-transaction-ids (get-in db [:wallet :swap-transaction-ids])]
     {:db (cond-> db
            :always                     (assoc-in [:wallet :transactions]
                                         (merge transactions transaction-details))
            :always                     (assoc-in [:wallet :ui :swap :transaction-ids] transaction-ids)
            approval-transaction?       (assoc-in [:wallet :ui :swap :approval-transaction-id]
                                         transaction-id)
            (not approval-transaction?) (assoc-in [:wallet :swap-transaction-ids]
                                         (if swap-transaction-ids
                                           (conj swap-transaction-ids transaction-id)
                                           (hash-set transaction-id))))})))

(rf/reg-event-fx :wallet.swap/approve-transaction-update
 (fn [{:keys [db]} [{:keys [status]}]]
   (let [{:keys [amount asset-to-pay swap-proposal]} (get-in db [:wallet :ui :swap])
         provider-name                               (:bridge-name swap-proposal)
         token-symbol                                (:symbol asset-to-pay)
         current-viewing-account-address             (get-in db
                                                             [:wallet :current-viewing-account-address])
         account-name                                (get-in db
                                                             [:wallet :accounts
                                                              current-viewing-account-address :name])
         transaction-confirmed-or-failed?            (#{:confirmed :failed} status)
         transaction-confirmed?                      (= status :confirmed)]
     (when transaction-confirmed-or-failed?
       (cond-> {:fx
                [[:dispatch
                  [:toasts/upsert
                   {:id   :approve-transaction-update
                    :type (if transaction-confirmed? :positive :negative)
                    :text (if transaction-confirmed?
                            (i18n/label :t/spending-cap-set
                                        {:amount        amount
                                         :token-symbol  token-symbol
                                         :provider-name provider-name
                                         :account-name  account-name})
                            (i18n/label :t/spending-cap-failed
                                        {:amount        amount
                                         :token-symbol  token-symbol
                                         :provider-name provider-name
                                         :account-name  account-name}))}]]]}
         (not transaction-confirmed?)
         (assoc :db (update-in db [:wallet :ui :swap] dissoc :approval-transaction-id)))))))

(rf/reg-event-fx :wallet.swap/swap-transaction-update
 (fn [{:keys [db]} [{:keys [tx-hash status]}]]
   (let [{:keys [pay-amount pay-token-symbol
                 receive-amount receive-token-symbol]} (get-in db
                                                               [:wallet :transactions tx-hash
                                                                :swap-data])
         transaction-confirmed-or-failed?              (#{:confirmed :failed} status)
         transaction-confirmed?                        (= status :confirmed)]
     (when transaction-confirmed-or-failed?
       {:db (-> db
                (update-in [:wallet :swap-transaction-ids] disj tx-hash)
                (update-in [:wallet :transactions] dissoc tx-hash))
        :fx [[:dispatch
              [:toasts/upsert
               {:id   :swap-transaction-update
                :type (if transaction-confirmed? :positive :negative)
                :text (if transaction-confirmed?
                        (i18n/label :t/swapped-to
                                    {:pay-amount           pay-amount
                                     :pay-token-symbol     pay-token-symbol
                                     :receive-token-symbol receive-token-symbol
                                     :receive-amount       receive-amount})
                        (i18n/label :t/swap-failed))}]]]}))))

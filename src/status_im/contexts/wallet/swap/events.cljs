(ns status-im.contexts.wallet.swap.events
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]
            [status-im.contexts.wallet.swap.utils :as swap-utils]
            [taoensso.timbre :as log]
            [utils.address :as address]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.number :as number]))

(rf/reg-event-fx :wallet.swap/start
 (fn [{:keys [db]} [{:keys [network asset-to-receive open-new-screen?] :as data}]]
   (let [{:keys [wallet]}       db
         test-networks-enabled? (get-in db [:profile/profile :test-networks-enabled?])
         view-id                (:view-id db)
         account                (swap-utils/wallet-account wallet)
         asset-to-pay           (if (get-in data [:asset-to-pay :networks])
                                  (:asset-to-pay data)
                                  (swap-utils/select-asset-to-pay-by-symbol
                                   {:wallet                 wallet
                                    :account                account
                                    :test-networks-enabled? test-networks-enabled?
                                    :token-symbol           (get-in data [:asset-to-pay :symbol])}))
         network'               (or network
                                    (swap-utils/select-network asset-to-pay))
         start-point            (if open-new-screen? :action-menu :swap-button)]
     {:db (-> db
              (assoc-in [:wallet :ui :swap :asset-to-pay] asset-to-pay)
              (assoc-in [:wallet :ui :swap :asset-to-receive] asset-to-receive)
              (assoc-in [:wallet :ui :swap :network] network')
              (assoc-in [:wallet :ui :swap :launch-screen] view-id)
              (assoc-in [:wallet :ui :swap :start-point] start-point))
      :fx (if network'
            [[:dispatch [:wallet/switch-current-viewing-account (:address account)]]
             [:dispatch
              (if open-new-screen?
                [:open-modal :screen/wallet.setup-swap]
                [:navigate-to-within-stack
                 [:screen/wallet.setup-swap :screen/wallet.swap-select-asset-to-pay]])]
             [:dispatch
              [:centralized-metrics/track :metric/swap-start
               {:network       (:chain-id network)
                :pay_token     (:symbol asset-to-pay)
                :receive_token (:symbol asset-to-receive)
                :start_point   start-point
                :launch_screen view-id}]]
             [:dispatch [:wallet.swap/set-default-slippage]]]
            [[:dispatch
              [:show-bottom-sheet
               {:content (fn []
                           [network-selection/view
                            {:token-symbol      (:symbol asset-to-pay)
                             :on-select-network (fn [network]
                                                  (rf/dispatch [:hide-bottom-sheet])
                                                  (rf/dispatch
                                                   [:wallet.swap/start
                                                    {:asset-to-pay     asset-to-pay
                                                     :asset-to-receive asset-to-receive
                                                     :network          network
                                                     :open-new-screen? open-new-screen?}]))}])}]]])})))

(rf/reg-event-fx :wallet.swap/select-asset-to-pay
 (fn [{:keys [db]} [{:keys [token]}]]
   (let [previous-token (get-in db [:wallet :ui :swap :asset-to-pay])
         network        (get-in db [:wallet :ui :swap :network])]
     {:db (update-in db
                     [:wallet :ui :swap]
                     #(-> %
                          (assoc :asset-to-pay token)
                          (dissoc :amount
                                  :amount-hex
                                  :last-request-uuid
                                  :swap-proposal
                                  :error-response
                                  :loading-swap-proposal?
                                  :approval-transaction-id
                                  :approved-amount)))
      :fx [[:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-pay-changed
             {:network        (:chain-id network)
              :previous_token (:symbol previous-token)
              :new_token      (:symbol token)}]]]})))

(rf/reg-event-fx :wallet.swap/select-asset-to-receive
 (fn [{:keys [db]} [{:keys [token]}]]
   (let [previous-token (get-in db [:wallet :ui :swap :asset-to-receive])
         network        (get-in db [:wallet :ui :swap :network])]
     {:db (assoc-in db [:wallet :ui :swap :asset-to-receive] token)
      :fx [[:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-receive-changed
             {:network        (:chain-id network)
              :previous_token (:symbol previous-token)
              :new_token      (:symbol token)}]]]})))

(rf/reg-event-fx :wallet.swap/set-default-slippage
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :swap :max-slippage] constants/default-slippage)}))

(rf/reg-event-fx :wallet.swap/set-max-slippage
 (fn [{:keys [db]} [max-slippage]]
   {:db (assoc-in db [:wallet :ui :swap :max-slippage] (number/parse-float max-slippage))}))

(rf/reg-event-fx :wallet/start-get-swap-proposal
 (fn [{:keys [db]} [{:keys [amount-in amount-out clean-approval-transaction?]}]]
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
                                  #(cond-> %
                                     :always
                                     (assoc
                                      :last-request-uuid      request-uuid
                                      :amount                 amount
                                      :amount-hex             amount-in-hex
                                      :loading-swap-proposal? true
                                      :initial-response?      true)
                                     clean-approval-transaction?
                                     (dissoc :approval-transaction-id :approved-amount :swap-proposal)))
        :fx            [[:dispatch
                         [:centralized-metrics/track :metric/swap-proposal-start
                          {:network       swap-chain-id
                           :pay_token     pay-token-id
                           :receive_token receive-token-id}]]]
        :json-rpc/call [{:method   "wallet_getSuggestedRoutesAsync"
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
         amount-hex        (get-in db [:wallet :ui :swap :amount-hex])
         asset-to-pay      (get-in db [:wallet :ui :swap :asset-to-pay])
         asset-to-receive  (get-in db [:wallet :ui :swap :asset-to-receive])
         network           (get-in db [:wallet :ui :swap :network])
         initial-response? (get-in db [:wallet :ui :swap :initial-response?])
         view-id           (:view-id db)
         request-uuid      (:uuid swap-proposal)
         best-routes       (:best swap-proposal)
         error-response    (:error-response swap-proposal)]
     (when (and (= request-uuid last-request-uuid)
                (or (and (empty? best-routes) error-response)
                    (and
                     (pos? (count best-routes))
                     (= (:amount-in (first best-routes)) amount-hex))))
       (cond-> {:db (update-in db
                               [:wallet :ui :swap]
                               assoc
                               :swap-proposal          (when-not (empty? best-routes)
                                                         (assoc (first best-routes) :uuid request-uuid))
                               :error-response         error-response
                               :loading-swap-proposal? false
                               :initial-response?      false)}
         (and initial-response? (seq best-routes))
         (assoc :fx
                [[:dispatch
                  [:centralized-metrics/track :metric/swap-proposal-received
                   {:network       (:chain-id network)
                    :pay_token     (:symbol asset-to-pay)
                    :receive_token (:symbol asset-to-receive)}]]])
         (and initial-response? (empty? best-routes))
         (assoc :fx
                [[:dispatch
                  [:centralized-metrics/track :metric/swap-proposal-failed
                   {:error (:code error-response)}]]])
         ;; Router is unstable and it can return a swap proposal and after auto-refetching it can
         ;; return an error. Ideally this shouldn't happen, but adding this behavior so if the
         ;; user is in swap confirmation screen or in token approval confirmation screen, we
         ;; navigate back to setup swap screen so proper error is displayed.
         (and (empty? best-routes) (= view-id :screen/wallet.swap-set-spending-cap))
         (assoc :fx
                [[:dispatch
                  [:centralized-metrics/track :metric/swap-proposal-failed
                   {:error (:code error-response)}]]
                 [:dismiss-modal :screen/wallet.swap-set-spending-cap]])
         (and (empty? best-routes) (= view-id :screen/wallet.swap-confirmation))
         (assoc :fx
                [[:dispatch
                  [:centralized-metrics/track :metric/swap-proposal-failed
                   {:error (:code error-response)}]]
                 [:navigate-back]]))))))

(rf/reg-event-fx :wallet/swap-proposal-error
 (fn [{:keys [db]} [error-response]]
   {:db (-> db
            (update-in [:wallet :ui :swap] dissoc :route :swap-proposal)
            (assoc-in [:wallet :ui :swap :loading-swap-proposal?] false)
            (assoc-in [:wallet :ui :swap :error-response] error-response))
    :fx [[:dispatch
          [:centralized-metrics/track :metric/swap-proposal-failed {:error (:code error-response)}]]]}))

(rf/reg-event-fx :wallet/stop-get-swap-proposal
 (fn []
   {:json-rpc/call [{:method   "wallet_stopSuggestedRoutesAsyncCalculation"
                     :params   []
                     :on-error (fn [error]
                                 (log/error "failed to stop fetching swap proposals"
                                            {:event :wallet/stop-get-swap-proposal
                                             :error error}))}]}))

(rf/reg-event-fx
 :wallet/clean-swap-proposal
 (fn [{:keys [db]} [{:keys [clean-approval-transaction?]}]]
   (let [keys-to-dissoc (cond-> [:amount
                                 :amount-hex
                                 :last-request-uuid
                                 :swap-proposal
                                 :error-response
                                 :loading-swap-proposal?]
                          clean-approval-transaction? (conj :approval-transaction-id :approved-amount))]
     {:db (apply update-in db [:wallet :ui :swap] dissoc keys-to-dissoc)
      :fx [[:dispatch [:wallet/stop-get-swap-proposal]]]})))

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
                                             receive-amount
                                             (when amount-out
                                               (-> amount-out
                                                   (number/hex->whole receive-token-decimals)
                                                   (money/to-fixed receive-token-decimals)))]
                                         (rf/dispatch [:centralized-metrics/track
                                                       (if approval-required?
                                                         :metric/swap-approval-execution-start
                                                         :metric/swap-transaction-execution-start)
                                                       (cond-> {:network   swap-chain-id
                                                                :pay_token token-id-from}
                                                         (not approval-required?)
                                                         (assoc :receive_token token-id-to))])
                                         (rf/dispatch [:wallet.swap/add-authorized-transaction
                                                       (cond-> {:transaction result
                                                                :approval-transaction?
                                                                approval-required?}
                                                         (not approval-required?)
                                                         (assoc :swap-data
                                                                {:pay-token-symbol     token-id-from
                                                                 :pay-amount           amount
                                                                 :receive-token-symbol token-id-to
                                                                 :receive-amount       receive-amount
                                                                 :swap-chain-id        swap-chain-id}))])
                                         (rf/dispatch [:hide-bottom-sheet])
                                         (rf/dispatch [:dismiss-modal
                                                       (if approval-required?
                                                         :screen/wallet.swap-set-spending-cap
                                                         :screen/wallet.swap-confirmation)])
                                         (when-not approval-required?
                                           (rf/dispatch [:wallet/end-swap-flow])
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
                                     (rf/dispatch [:centralized-metrics/track
                                                   (if approval-required?
                                                     :metric/swap-approval-execution-failed
                                                     :metric/swap-transaction-execution-failed)
                                                   (cond-> {:network   swap-chain-id
                                                            :error     error
                                                            :pay_token token-id-from}
                                                     (not approval-required?)
                                                     (assoc :receive_token token-id-to))])
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
   (let [{:keys [amount asset-to-pay swap-proposal
                 network]}                (get-in db [:wallet :ui :swap])
         provider-name                    (:bridge-name swap-proposal)
         token-symbol                     (:symbol asset-to-pay)
         swap-chain-id                    (:chain-id network)
         current-viewing-account-address  (get-in db
                                                  [:wallet :current-viewing-account-address])
         account-name                     (get-in db
                                                  [:wallet :accounts
                                                   current-viewing-account-address :name])
         transaction-confirmed-or-failed? (#{:confirmed :failed} status)
         transaction-confirmed?           (= status :confirmed)]
     (when transaction-confirmed-or-failed?
       (cond-> {:fx
                [[:dispatch
                  [:centralized-metrics/track :metric/swap-approval-execution-finished
                   {:network   swap-chain-id
                    :pay_token token-symbol
                    :succeeded transaction-confirmed?}]]
                 [:dispatch
                  [:toasts/upsert
                   {:id   :approve-transaction-update
                    :type (if transaction-confirmed? :positive :negative)
                    :text (if transaction-confirmed?
                            (i18n/label :t/spending-cap-set
                                        {:token-amount  amount
                                         :token-symbol  token-symbol
                                         :provider-name provider-name
                                         :account-name  account-name})
                            (i18n/label :t/spending-cap-failed
                                        {:token-amount  amount
                                         :token-symbol  token-symbol
                                         :provider-name provider-name
                                         :account-name  account-name}))}]]]}
         transaction-confirmed?
         (assoc :db (assoc-in db [:wallet :ui :swap :approved-amount] amount))
         (not transaction-confirmed?)
         (assoc :db (update-in db [:wallet :ui :swap] dissoc :approval-transaction-id)))))))

(rf/reg-event-fx :wallet.swap/swap-transaction-update
 (fn [{:keys [db]} [{:keys [tx-hash status]}]]
   (let [{:keys [pay-amount pay-token-symbol
                 receive-amount receive-token-symbol
                 swap-chain-id]}          (get-in db
                                                  [:wallet :transactions tx-hash
                                                   :swap-data])
         transaction-confirmed-or-failed? (#{:confirmed :failed} status)
         transaction-confirmed?           (= status :confirmed)]
     (when transaction-confirmed-or-failed?
       {:db (-> db
                (update-in [:wallet :swap-transaction-ids] disj tx-hash)
                (update-in [:wallet :transactions] dissoc tx-hash))
        :fx [[:dispatch
              [:centralized-metrics/track :metric/swap-transaction-execution-finished
               {:network       swap-chain-id
                :pay_token     pay-token-symbol
                :receive_token receive-token-symbol
                :succeeded     transaction-confirmed?}]]
             [:dispatch
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

(rf/reg-event-fx :wallet.swap/flip-assets
 (fn [{:keys [db]}]
   (let [{:keys [asset-to-pay asset-to-receive
                 swap-proposal amount network]} (get-in db [:wallet :ui :swap])
         receive-token-decimals                 (:decimals asset-to-receive)
         amount-out                             (when swap-proposal (:amount-out swap-proposal))
         receive-amount                         (when amount-out
                                                  (-> amount-out
                                                      (number/hex->whole receive-token-decimals)
                                                      (money/to-fixed receive-token-decimals)))]
     {:db (update-in db
                     [:wallet :ui :swap]
                     #(-> %
                          (assoc
                           :asset-to-pay     asset-to-receive
                           :asset-to-receive asset-to-pay
                           :amount           (or receive-amount amount))
                          (dissoc :swap-proposal
                                  :error-response
                                  :loading-swap-proposal?
                                  :last-request-uuid
                                  :approved-amount
                                  :approval-transaction-id)))
      :fx [[:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-pay-changed
             {:network        (:chain-id network)
              :previous_token (:symbol asset-to-pay)
              :new_token      (:symbol asset-to-receive)}]]
           [:dispatch
            [:centralized-metrics/track :metric/swap-asset-to-receive-changed
             {:network        (:chain-id network)
              :previous_token (:symbol asset-to-receive)
              :new_token      (:symbol asset-to-pay)}]]]})))

(rf/reg-event-fx :wallet/end-swap-flow
 (fn [{:keys [db]}]
   (let [launch-screen (get-in db [:wallet :ui :swap :launch-screen])
         address       (get-in db [:wallet :current-viewing-account-address])]
     {:fx [(when (= launch-screen :wallet-stack)
             [:dispatch [:wallet/navigate-to-account-within-stack address]])
           [:dispatch [:wallet/fetch-activities-for-current-account]]
           [:dispatch [:wallet/select-account-tab :activity]]]})))

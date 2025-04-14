(ns status-im.contexts.wallet.send.events
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as collectible.utils]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.data-store :as data-store]
    [status-im.contexts.wallet.db :as db]
    [status-im.contexts.wallet.db-path :as db-path]
    [status-im.contexts.wallet.networks.db :as networks.db]
    [status-im.contexts.wallet.send.transaction-settings.core :as transaction-settings]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.contexts.wallet.sheets.network-selection.view :as network-selection]
    [taoensso.timbre :as log]
    [utils.address]
    [utils.i18n :as i18n]
    [utils.number]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.signatures :as signatures]))

(rf/reg-event-fx :wallet/clean-send-data
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :send)}))

(rf/reg-event-fx :wallet/select-address-tab
 (fn [{:keys [db]} [tab]]
   {:db (update-in db db-path/send assoc :select-address-tab tab)}))

(rf/reg-event-fx :wallet/stop-and-clean-suggested-routes
 (fn []
   {:fx [[:dispatch [:wallet/stop-get-suggested-routes]]
         [:dispatch [:wallet/clean-suggested-routes]]]}))

(defn- add-not-enough-assets-data
  [send-data chosen-route to-network-amounts-by-chain]
  (-> send-data
      (assoc :route                     chosen-route
             :loading-suggested-routes? false
             :suggested-routes          {:best []}
             :enough-assets?            false
             :to-values-by-chain        to-network-amounts-by-chain)
      (update :sender-network-values send-utils/reset-loading-network-amounts-to-zero)
      (update :receiver-network-values send-utils/reset-loading-network-amounts-to-zero)))

(rf/reg-event-fx
 :wallet/suggested-routes-success
 (fn [{:keys [db]} [suggested-routes-data enough-assets?]]
   (let [chosen-route                            (:best suggested-routes-data)
         {:keys [token collectible token-display-name
                 receiver-network-values
                 sender-network-values tx-type]} (get-in db db-path/send)
         token-decimals                          (if collectible 0 (:decimals token))
         native-token?                           (and token (= token-display-name "ETH"))
         to-network-amounts-by-chain             (send-utils/network-amounts-by-chain
                                                  {:route          chosen-route
                                                   :token-decimals token-decimals
                                                   :native-token?  native-token?
                                                   :receiver?      true})]
     (if-not enough-assets?
       {:db (update-in db
                       db-path/send
                       add-not-enough-assets-data
                       chosen-route
                       to-network-amounts-by-chain)}
       (let [routes-available?             (pos? (count chosen-route))
             from-network-amounts-by-chain (send-utils/network-amounts-by-chain
                                            {:route          chosen-route
                                             :token-decimals token-decimals
                                             :native-token?  native-token?
                                             :receiver?      false})
             sender-possible-chain-ids     (map :chain-id sender-network-values)
             sender-network-values         (if routes-available?
                                             (send-utils/network-amounts
                                              (if (= tx-type :tx/bridge)
                                                from-network-amounts-by-chain
                                                (send-utils/add-zero-values-to-network-values
                                                 from-network-amounts-by-chain
                                                 sender-possible-chain-ids)))
                                             (send-utils/reset-loading-network-amounts-to-zero
                                              sender-network-values))
             receiver-network-values       (if routes-available?
                                             (send-utils/network-amounts
                                              to-network-amounts-by-chain)
                                             (send-utils/reset-loading-network-amounts-to-zero
                                              receiver-network-values))
             network-links                 (when routes-available?
                                             (send-utils/network-links chosen-route
                                                                       sender-network-values
                                                                       receiver-network-values))]
         {:db (update-in db
                         db-path/send
                         assoc
                         :suggested-routes          suggested-routes-data
                         :route                     chosen-route
                         :from-values-by-chain      from-network-amounts-by-chain
                         :to-values-by-chain        to-network-amounts-by-chain
                         :sender-network-values     sender-network-values
                         :receiver-network-values   receiver-network-values
                         :network-links             network-links
                         :loading-suggested-routes? false
                         :enough-assets?            true)})))))

(rf/reg-event-fx
 :wallet/suggested-routes-error
 (fn [{:keys [db]} [error-message]]
   (let [{:keys [sender-network-values receiver-network-values]} (get-in db db-path/send)]
     {:db (-> db
              (update-in db-path/send dissoc :route)
              (update-in db-path/send
                         assoc
                         :sender-network-values
                         (send-utils/reset-loading-network-amounts-to-zero sender-network-values)
                         :receiver-network-values
                         (send-utils/reset-loading-network-amounts-to-zero receiver-network-values)
                         :loading-suggested-routes? false
                         :suggested-routes {:best []}))
      :fx [[:dispatch
            [:toasts/upsert
             {:id   :send-transaction-error
              :type :negative
              :text error-message}]]]})))

(rf/reg-event-fx :wallet/clean-send-address
 (fn [{:keys [db]}]
   {:db (update-in db db-path/send dissoc :recipient :to-address)}))

(rf/reg-event-fx :wallet/clean-send-amount
 (fn [{:keys [db]}]
   {:db (update-in db db-path/send dissoc :amount)}))

(rf/reg-event-fx
 :wallet/init-send-flow-for-address
 (fn [{:keys [db]} [{:keys [recipient address stack-id]}]]
   (let [address                 (utils.address/extract-address-without-chains-info address)
         wallet-accounts         (vals (get-in db [:wallet :accounts]))
         default-account-address (some #(when (:default-account? %) (:address %))
                                       wallet-accounts)
         multiple-accounts?      (-> (filter :operable? wallet-accounts)
                                     count
                                     (> 1))]
     {:db (cond-> (update-in db [:wallet :ui] dissoc :send)
            (not multiple-accounts?)
            (assoc-in [:wallet :current-viewing-account-address] default-account-address)

            :always
            (update-in
             db-path/send
             assoc
             :general-flow? true
             :recipient     (or recipient address)
             :to-address    address))
      :fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:shell/change-tab :screen/wallet-stack]]
           [:dispatch [:pop-to-root :screen/shell-stack]]
           [:dispatch-later
            [{:ms       600
              :dispatch (if multiple-accounts?
                          [:open-modal :screen/wallet.select-from]
                          [:wallet/wizard-navigate-forward
                           {:current-screen stack-id
                            :start-flow?    true
                            :flow-id        :wallet-send-flow}])}]]]})))

(rf/reg-event-fx
 :wallet/select-send-address
 (fn [{:keys [db]} [{:keys [address recipient stack-id start-flow?]}]]
   (let [address          (utils.address/extract-address-without-chains-info address)
         sender           (get-in db [:wallet :current-viewing-account-address])
         collectible-tx?  (send-utils/tx-type-collectible? (-> db db/send :tx-type))
         collectible      (when collectible-tx? (-> db db/send :collectible))
         one-collectible? (when collectible-tx?
                            (= (collectible.utils/collectible-balance collectible sender) 1))]
     {:db (update-in db
                     db-path/send
                     assoc
                     :recipient  (or recipient address)
                     :to-address address)
      :fx [(when (and collectible-tx? one-collectible?)
             [:dispatch [:wallet/start-get-suggested-routes {:amount 1}]])
           [:dispatch
            [:wallet/wizard-navigate-forward
             {:current-screen stack-id
              :start-flow?    start-flow?
              :flow-id        :wallet-send-flow}]]]})))

(rf/reg-event-fx
 :wallet/update-receiver-networks
 (fn [{:keys [db]} [selected-networks]]
   (let [amount (get-in db (conj db-path/send :amount))]
     {:db (update-in db db-path/send assoc :receiver-networks selected-networks)
      :fx [[:dispatch [:wallet/start-get-suggested-routes {:amount amount}]]]})))

(rf/reg-event-fx
 :wallet/set-token-to-send
 (fn [{:keys [db]}
      [{:keys [token-symbol token network stack-id start-flow? owners] :as params} entry-point]]
   ;; `token` is a map extracted from the sender, but in the wallet home page we don't know the
   ;; sender yet, so we only provide the `token-symbol`, later in
   ;; `:wallet/select-from-account` the `token` key will be set.
   (let [{:keys [wallet]}             db
         unique-owner                 (when (= (count owners) 1)
                                        (first owners))
         unique-owner-tokens          (get-in db [:wallet :accounts unique-owner :tokens])
         token-data                   (or token
                                          (when (and token-symbol unique-owner)
                                            (some #(when (= (:symbol %) token-symbol) %)
                                                  unique-owner-tokens)))
         view-id                      (:view-id db)
         root-screen?                 (or (= view-id :screen/wallet-stack) (nil? view-id))
         multi-account-balance?       (-> (utils/get-accounts-with-token-balance (:accounts wallet)
                                                                                 token)
                                          (count)
                                          (> 1))
         account-not-defined?         (and (not unique-owner) multi-account-balance?)
         networks-with-balance        (when (and token-data (:balances-per-chain token))
                                        (filter #(not= (:balance %) "0")
                                                (vals (:balances-per-chain token))))
         balance-in-only-one-network? (when networks-with-balance (= (count networks-with-balance) 1))
         network-details              (networks.db/get-networks db)
         network                      (if balance-in-only-one-network?
                                        (first (filter #(= (:chain-id %)
                                                           (:chain-id (first networks-with-balance)))
                                                       network-details))
                                        network)]
     (when (or token-data token-symbol)
       {:db (cond-> db
              network      (update-in db-path/send
                                      #(-> %
                                           (dissoc :collectible)
                                           (assoc :network network)))
              token-symbol (update-in db-path/send assoc :token-symbol token-symbol)
              token-data   (update-in db-path/send
                                      #(assoc %
                                              :token              (assoc token-data
                                                                         :supported-networks
                                                                         (network-utils/network-list
                                                                          token-data
                                                                          network-details))
                                              :token-display-name (:symbol token-data)
                                              :token-symbol       (:symbol token-data)))
              unique-owner (assoc-in [:wallet :current-viewing-account-address] unique-owner)
              entry-point  (update-in db-path/send assoc :entry-point entry-point)
              :always      (update-in db-path/send assoc :tx-type :tx/send))
        :fx (cond
              ;; If the token has a balance in more than one account and this was dispatched from
              ;; the general wallet screen, open the account selection screen.
              (and account-not-defined? root-screen?)
              [[:dispatch [:open-modal :screen/wallet.select-from]]]

              ;; If the token has a balance in only one account (or this was dispatched from the
              ;; account screen) and the network is already set, stop and clean suggested routes,
              ;; then navigate forward in the send flow.
              (some? network)
              [[:dispatch [:wallet/stop-and-clean-suggested-routes]]
               [:dispatch
                ;; ^:flush-dom allows us to make sure the re-frame DB state is always synced
                ;; before the navigation occurs, so the new screen is always rendered with
                ;; the DB state set by this event. By adding the metadata we are omitting
                ;; a 1-frame blink when the screen is mounted.
                ^:flush-dom
                [:wallet/wizard-navigate-forward
                 {:current-screen stack-id
                  :start-flow?    start-flow?
                  :flow-id        :wallet-send-flow}]]]

              ;; If we don't know the network but need to set it, show the network selection drawer.
              :else
              [[:dispatch
                [:show-bottom-sheet
                 {:content (fn []
                             [network-selection/view
                              {:token-symbol      (or token-symbol (:symbol token-data))
                               :source            :send
                               :on-select-network (fn [network]
                                                    (rf/dispatch [:hide-bottom-sheet])
                                                    (rf/dispatch
                                                     [:wallet/set-token-to-send
                                                      (assoc params :network network)]))}])}]]])}))))

(rf/reg-event-fx
 :wallet/edit-token-to-send
 (fn [{:keys [db]} [token]]
   (let [{token-networks :supported-networks
          token-symbol   :symbol}                  token
         receiver-networks                         (get-in db (conj db-path/send :receiver-networks))
         token-networks-ids                        (map :chain-id token-networks)
         token-not-supported-in-receiver-networks? (not-any? (set receiver-networks)
                                                             token-networks-ids)]
     {:db (update-in db
                     db-path/send
                     assoc
                     :token token
                     :token-display-name token-symbol
                     :token-not-supported-in-receiver-networks?
                     token-not-supported-in-receiver-networks?)
      :fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:wallet/stop-and-clean-suggested-routes]]]})))

(rf/reg-event-fx :wallet/clean-selected-token
 (fn [{:keys [db]}]
   {:db (update-in db
                   db-path/send
                   dissoc
                   :token
                   :token-symbol
                   :token-display-name
                   :network)}))

(rf/reg-event-fx :wallet/clean-selected-collectible
 (fn [{:keys [db]} [{:keys [ignore-entry-point?]}]]
   (let [{:keys [entry-point collectible-multiple-owners? tx-type]} (get-in db db-path/send)
         entry-point-wallet-home?                                   (= entry-point :screen/wallet-stack)
         multiple-owners?                                           collectible-multiple-owners?
         transaction-type                                           tx-type]
     (when (or ignore-entry-point?
               (and entry-point-wallet-home? (not multiple-owners?))
               (not entry-point-wallet-home?))
       {:db (update-in db
                       db-path/send
                       dissoc
                       :collectible
                       :collectible-multiple-owners?
                       :token-display-name
                       :amount
                       (when (send-utils/tx-type-collectible? transaction-type)
                         :tx-type))}))))

(rf/reg-event-fx
 :wallet/set-collectible-to-send
 (fn [{db :db} [{:keys [collectible current-screen start-flow? entry-point]}]]
   (let [viewing-account?   (some? (-> db :wallet :current-viewing-account-address))
         entry-point        (cond
                              entry-point      entry-point
                              viewing-account? :account-collectible-tab
                              :else            :screen/wallet-stack)
         collection-data    (:collection-data collectible)
         collectible-data   (:collectible-data collectible)
         contract-type      (:contract-type collectible)
         tx-type            (if (= contract-type constants/wallet-contract-type-erc-1155)
                              :tx/collectible-erc-1155
                              :tx/collectible-erc-721)
         collectible-id     (get-in collectible [:id :token-id])
         single-owner?      (-> collectible :ownership count (= 1))
         owner-address      (-> collectible :ownership first :address)
         one-collectible?   (when single-owner?
                              (= (collectible.utils/collectible-balance collectible owner-address) 1))
         token-display-name (cond
                              (and collectible
                                   (not (string/blank? (:name collectible-data))))
                              (:name collectible-data)

                              collectible
                              (str (:name collection-data) " #" collectible-id))
         collectible-tx     (-> db
                                (update-in db-path/send dissoc :token)
                                (update-in db-path/send
                                           assoc
                                           :entry-point entry-point
                                           :collectible collectible
                                           :collectible-multiple-owners?
                                           (not single-owner?)
                                           :token-display-name token-display-name
                                           :tx-type tx-type))
         recipient-set?     (-> db db/send :recipient)]
     {:db (cond-> collectible-tx

            (and (not viewing-account?) single-owner?)
            (assoc-in [:wallet :current-viewing-account-address] owner-address)

            one-collectible?
            (update-in db-path/send assoc :amount 1))
      :fx (if
            ;; If the collectible is present in multiple accounts, the user will be taken to select
            ;; the address to send from
            (and (not viewing-account?) (not single-owner?))
            [[:dispatch [:open-modal :screen/wallet.select-from]]]

            [(when (and one-collectible? recipient-set?)
               [:dispatch [:wallet/start-get-suggested-routes {:amount 1}]])
             [:dispatch
              [:wallet/wizard-navigate-forward
               {:current-screen current-screen
                :start-flow?    start-flow?
                :flow-id        :wallet-send-flow}]]])})))

(rf/reg-event-fx
 :wallet/set-collectible-amount-to-send
 (fn [{db :db} [{:keys [stack-id amount]}]]
   {:db (update-in db db-path/send assoc :amount amount)
    :fx [[:dispatch [:wallet/start-get-suggested-routes {:amount amount}]]
         [:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :flow-id        :wallet-send-flow}]]]}))

(rf/reg-event-fx
 :wallet/set-token-amount-to-send
 (fn [{:keys [db]} [{:keys [amount stack-id start-flow?]}]]
   {:db (update-in db db-path/send assoc :amount amount)
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :start-flow?    start-flow?
            :flow-id        :wallet-send-flow}]]]}))

(rf/reg-event-fx
 :wallet.send/auth-slider-completed
 (fn [{:keys [db]}]
   (let [last-request-uuid (get-in db (conj db-path/send :last-request-uuid))]
     {:db (update-in db db-path/send dissoc :transaction-for-signing)
      :fx [[:dispatch [:wallet/build-transactions-from-route {:request-uuid last-request-uuid}]]
           [:dispatch
            [:wallet.send/set-sign-transactions-callback-fx
             [:dispatch
              [:wallet/prepare-signatures-for-transactions :send]]]]]})))

(defn log-transaction-signature-error
  [error]
  (log/error
   "failed to prepare signatures for transactions"
   {:event :wallet/prepare-signatures-for-transactions
    :error error})
  (rf/dispatch
   [:toasts/upsert
    {:id   :prepare-signatures-for-transactions-error
     :type :negative
     :text (:message error)}]))

(defn- send-transactions-with-signatures
  [tx-type signatures]
  (rf/dispatch
   [:wallet/send-router-transactions-with-signatures tx-type
    signatures]))

(rf/reg-event-fx
 :wallet/send-router-transactions-with-signatures
 (fn [{:keys [db]} [tx-type signatures]]
   (let [transaction-for-signing (get-in db [:wallet :ui tx-type :transaction-for-signing])
         signatures-map          (reduce (fn [acc {:keys [message signature]}]
                                           (assoc acc
                                                  message
                                                  (signatures/signature->rsv signature)))
                                         {}
                                         signatures)]
     {:json-rpc/call [{:method     "wallet_sendRouterTransactionsWithSignatures"
                       :params     [{:uuid       (get-in transaction-for-signing [:sendDetails :uuid])
                                     :signatures signatures-map}]
                       :on-success (fn []
                                     (rf/dispatch [:hide-bottom-sheet]))
                       :on-error   (fn [error]
                                     (log/error "failed to send router transactions with signatures"
                                                {:event :wallet/send-router-transactions-with-signatures
                                                 :error error})
                                     (rf/dispatch [:toasts/upsert
                                                   {:id   :send-router-transactions-with-signatures-error
                                                    :type :negative
                                                    :text (:message error)}]))}]})))

(defn- transaction-sign-payload
  [transaction-for-signing]
  (let [signing-details (:signingDetails transaction-for-signing)
        address         (:address signing-details)
        messages        (:hashes signing-details)]
    (reduce (fn [acc message]
              (conj acc
                    {:address address
                     :message message}))
            []
            messages)))

(rf/reg-event-fx
 :wallet/prepare-signatures-for-transactions
 (fn [{:keys [db]} [tx-type]]
   (let [sign-payload (-> db
                          (get-in [:wallet :ui tx-type :transaction-for-signing])
                          transaction-sign-payload)
         send-tx-fn   (partial send-transactions-with-signatures tx-type)]
     {:fx [[:dispatch
            [:standard-auth/authorize-and-sign
             {:sign-payload      sign-payload
              :auth-button-label (i18n/label :t/confirm)
              :on-sign-success   (fn [signatures]
                                   (send-tx-fn signatures))
              :on-sign-error     log-transaction-signature-error}]]]})))

(rf/reg-event-fx
 :wallet/standard-auth-autorization-success
 (fn [_ [hashes address sha3-pwd send-tx-fn]]
   {:fx [[:effects.wallet/sign-transaction-hashes
          {:hashes     hashes
           :address    address
           :password   (security/safe-unmask-data sha3-pwd)
           :on-success send-tx-fn
           :on-error   log-transaction-signature-error}]]}))

(rf/reg-event-fx
 :wallet/build-transaction-for-collectible-route
 (fn [{:keys [db]}]
   (let [{:keys [last-request-uuid collectible]} (get-in db db-path/send)
         collectible-unique-id                   (:unique-id collectible)]
     {:db (->
            db
            (update-in db-path/send dissoc :transaction-for-signing)
            (assoc-in [:wallet :ui :collectibles :pending collectible-unique-id] true))
      :fx [[:dispatch [:wallet/build-transactions-from-route {:request-uuid last-request-uuid}]]]})))

(rf/reg-event-fx
 :wallet/set-token-amount-to-bridge
 (fn [{:keys [db]} [{:keys [amount stack-id start-flow?]}]]
   {:db (-> db
            (update-in db-path/send assoc :amount amount)
            (update-in db-path/send dissoc :transaction-for-signing))
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :start-flow?    start-flow?
            :flow-id        :wallet-bridge-flow}]]]}))

(rf/reg-event-fx
 :wallet/clean-bridge-to-selection
 (fn [{:keys [db]}]
   {:db (update-in db db-path/send dissoc :bridge-to-chain-id)}))

(rf/reg-event-fx
 :wallet/clean-suggested-routes
 (fn [{:keys [db]}]
   (let [keys-to-remove [:to-values-by-chain :network-links :sender-network-values :route
                         :receiver-network-values :suggested-routes :from-values-by-chain
                         :loading-suggested-routes? :amount :enough-assets?]]
     {:db (update-in db db-path/send #(apply dissoc % keys-to-remove))})))

(rf/reg-event-fx :wallet/reset-network-amounts-to-zero
 (fn [{:keys [db]}]
   (let [{:keys [sender-network-values
                 receiver-network-values]} (get-in db db-path/send)
         sender-network-values             (send-utils/reset-network-amounts-to-zero
                                            sender-network-values)
         receiver-network-values           (send-utils/reset-network-amounts-to-zero
                                            receiver-network-values)]
     {:db (-> db
              (update-in db-path/send
                         assoc
                         :sender-network-values   sender-network-values
                         :receiver-network-values receiver-network-values)
              (update-in db-path/send
                         dissoc
                         :network-links
                         (when (empty? sender-network-values) :sender-network-values)
                         (when (empty? receiver-network-values) :receiver-network-values)))})))

(rf/reg-event-fx :wallet/start-get-suggested-routes
 (fn [{:keys [db]} [{:keys [amount amount-out updated-token] :as args :or {amount-out "0"}}]]
   (let [wallet-address                (get-in db [:wallet :current-viewing-account-address])
         {:keys [token tx-type collectible to-address
                 network bridge-to-chain-id]
          :or   {token updated-token}} (get-in db db-path/send)
         network-chain-ids             (networks.db/get-chain-ids db)
         token-decimal                 (when token (:decimals token))
         token-id                      (utils/format-token-id token collectible)
         to-token-id                   ""
         gas-rates                     constants/gas-rate-medium
         to-hex                        (fn [v] (send-utils/amount-in-hex v (if token token-decimal 0)))
         amount-in                     (to-hex amount)
         amount-out                    (to-hex amount-out)
         from-address                  wallet-address
         network-chain-id              (if collectible
                                         (get-in collectible [:id :contract-id :chain-id])
                                         (:chain-id network))
         disabled-from-chain-ids       (filter #(not= % network-chain-id) network-chain-ids)
         disabled-to-chain-ids         (filter #(not= %
                                                      (if (= tx-type :tx/bridge)
                                                        bridge-to-chain-id
                                                        network-chain-id))
                                               network-chain-ids)
         send-type                     (case tx-type
                                         :tx/collectible-erc-721  constants/send-type-erc-721-transfer
                                         :tx/collectible-erc-1155 constants/send-type-erc-1155-transfer
                                         :tx/bridge               constants/send-type-bridge
                                         constants/send-type-transfer)
         sender-network-values         (when (= tx-type :tx/bridge)
                                         (send-utils/loading-network-amounts
                                          {:networks  [network-chain-id]
                                           :values    {network-chain-id amount}
                                           :receiver? false}))
         receiver-network-values       (when (= tx-type :tx/bridge)
                                         (send-utils/loading-network-amounts
                                          {:networks  [bridge-to-chain-id]
                                           :receiver? true}))
         request-uuid                  (str (random-uuid))
         params                        [{:uuid                 request-uuid
                                         :sendType             send-type
                                         :addrFrom             from-address
                                         :addrTo               to-address
                                         :amountIn             amount-in
                                         :amountOut            amount-out
                                         :tokenID              token-id
                                         :toTokenID            to-token-id
                                         :disabledFromChainIDs disabled-from-chain-ids
                                         :disabledToChainIDs   disabled-to-chain-ids
                                         :gasFeeMode           gas-rates
                                         :fromLockedAmount     {}
                                         :username             (:username args)
                                         :publicKey            (:publicKey args)
                                         :packID               (:packID args)}]]
     (when (and to-address from-address amount-in token-id)
       {:db            (update-in db
                                  db-path/send
                                  #(-> %
                                       (assoc :last-request-uuid         request-uuid
                                              :amount                    amount
                                              :loading-suggested-routes? true
                                              :sender-network-values     sender-network-values
                                              :receiver-network-values   receiver-network-values)
                                       (dissoc :network-links :skip-processing-suggested-routes?)
                                       (cond-> token (assoc :token token))))
        :json-rpc/call [{:method   "wallet_getSuggestedRoutesAsync"
                         :params   params
                         :on-error (fn [error]
                                     (rf/dispatch [:wallet/suggested-routes-error error])
                                     (log/error "failed to get suggested routes (async)"
                                                {:event  :wallet/start-get-suggested-routes
                                                 :error  (:message error)
                                                 :params params}))}]}))))

(rf/reg-event-fx :wallet/stop-get-suggested-routes
 (fn [{:keys [db]}]
   ;; Adding a key to prevent processing route signals in the client until the routes generation is
   ;; stopped. This is to ensure no route signals are processed when we make the RPC call
   {:db (update-in db db-path/send assoc :skip-processing-suggested-routes? true)
    :fx [[:json-rpc/call
          [{:method   "wallet_stopSuggestedRoutesAsyncCalculation"
            :params   []
            :on-error (fn [error]
                        (log/error "failed to stop suggested routes calculation"
                                   {:event :wallet/stop-get-suggested-routes
                                    :error error}))}]]]}))

(rf/reg-event-fx
 :wallet/handle-suggested-routes
 (fn [{:keys [db]} [data]]
   (let [{send :send swap? :swap} (-> db :wallet :ui)
         skip-processing-routes?  (:skip-processing-suggested-routes? send)
         clean-user-tx-settings?  (get-in
                                   db
                                   (conj db-path/send :user-tx-settings :delete-on-routes-update?))]
     (when (or swap? (not skip-processing-routes?))
       (let [{error-code :code
              :as        error} (:ErrorResponse data)
             enough-assets?     (not (and (:Best data) (= error-code "WR-002")))
             failure?           (and error enough-assets? (not swap?))
             error-message      (if (zero? error-code) "An error occurred" (:details error))]
         (when failure?
           (log/error "failed to get suggested routes (async)"
                      {:event :wallet/handle-suggested-routes
                       :error error-message}))
         (merge
          (when clean-user-tx-settings?
            {:db (update-in db db-path/send dissoc :user-tx-settings)})
          {:fx [[:dispatch
                 (cond
                   (and failure? swap?) [:wallet/swap-proposal-error error]
                   failure?             [:wallet/suggested-routes-error error-message]
                   swap?                [:wallet/swap-proposal-success (data-store/fix-routes data)]
                   :else                [:wallet/suggested-routes-success (data-store/fix-routes data)
                                         enough-assets?])]]}))))))

(rf/reg-event-fx
 :wallet/transaction-success
 (fn [{:keys [db]} [sent-transactions]]
   (let [wallet-transactions (get-in db [:wallet :transactions] {})
         transactions        (utils/transactions->hash-to-transaction-map sent-transactions)
         transaction-ids     (->> transactions
                                  vals
                                  (map :hash))]
     {:db (-> db
              (update-in db-path/send
                         assoc
                         :just-completed-transaction? true
                         :transaction-ids             transaction-ids)
              (assoc-in [:wallet :transactions] (merge wallet-transactions transactions)))
      :fx [[:dispatch [:wallet/end-transaction-flow]]
           [:dispatch-later
            [{:ms       2000
              :dispatch [:wallet/stop-and-clean-suggested-routes]}]]
           [:dispatch-later
            [{:ms       2000
              :dispatch [:wallet/clean-just-completed-transaction]}]]]})))

(rf/reg-event-fx
 :wallet/transaction-failure
 (fn [_ [{:keys [details]}]]
   {:fx [[:dispatch [:wallet/end-transaction-flow]]
         [:dispatch-later
          [{:ms       2000
            :dispatch [:wallet/stop-and-clean-suggested-routes]}]]
         [:dispatch
          [:toasts/upsert
           {:id   :send-transaction-failure
            :type :negative
            :text (or details "An error occured")}]]]}))

(rf/reg-event-fx :wallet/clean-just-completed-transaction
 (fn [{:keys [db]}]
   {:db (update-in db db-path/send dissoc :just-completed-transaction?)}))

(rf/reg-event-fx :wallet/clean-up-transaction-flow
 (fn [_]
   {:fx [[:dispatch [:dismiss-modal :screen/wallet.transaction-confirmation]]
         [:dispatch [:dismiss-modal :screen/wallet.collectible]]
         [:dispatch [:wallet/clean-scanned-address]]
         [:dispatch [:wallet/clean-local-suggestions]]
         [:dispatch [:wallet/clean-send-address]]
         [:dispatch [:wallet/select-address-tab nil]]]}))

(rf/reg-event-fx :wallet/end-transaction-flow
 (fn [{:keys [db]}]
   (let [address (get-in db [:wallet :current-viewing-account-address])]
     {:fx [[:dispatch [:wallet/navigate-to-account-within-stack address]]
           [:dispatch [:wallet/select-account-tab :activity]]
           [:dispatch-later
            [{:ms       20
              :dispatch [:wallet/clean-up-transaction-flow]}]]]})))

(rf/reg-event-fx
 :wallet/build-transactions-from-route
 (fn [_ [{:keys [request-uuid slippage] :or {slippage constants/default-slippage}}]]
   {:json-rpc/call [{:method   "wallet_buildTransactionsFromRoute"
                     :params   [{:uuid               request-uuid
                                 :slippagePercentage slippage}]
                     :on-error (fn [error]
                                 (log/error "failed to build transactions from route"
                                            {:event :wallet/build-transactions-from-route
                                             :error error})
                                 (rf/dispatch [:toasts/upsert
                                               {:id   :build-transactions-from-route-error
                                                :type :negative
                                                :text (:message error)}]))}]}))

(rf/reg-event-fx
 :wallet/select-from-account
 (fn [{db :db} [{:keys [address stack-id network-details network start-flow?] :as params}]]
   (let [{:keys [token-symbol
                 tx-type]}            (db/send db)
         no-tx-type?                  (nil? tx-type)
         collectible-tx?              (send-utils/tx-type-collectible? tx-type)
         token                        (when token-symbol
                                        ;; When this flow has started in the wallet home page, we
                                        ;; know the token or collectible to send, but we don't know
                                        ;; from which account, so we extract the token data from
                                        ;; the picked account.
                                        (let [token (utils/get-token-from-account db
                                                                                  token-symbol
                                                                                  address)]
                                          (utils/token-with-balance token network-details)))
         asset-selected?              (or collectible-tx? (some? token))
         bridge-tx?                   (= tx-type :tx/bridge)
         flow-id                      (if bridge-tx?
                                        :wallet-bridge-flow
                                        :wallet-send-flow)
         networks-with-balance        (when (and token (:balances-per-chain token))
                                        (filter #(not= (:balance %) "0")
                                                (vals (:balances-per-chain token))))
         balance-in-only-one-network? (when networks-with-balance (= (count networks-with-balance) 1))
         network-details              (networks.db/get-networks db)
         network                      (if balance-in-only-one-network?
                                        (first (filter #(= (:chain-id %)
                                                           (:chain-id (first networks-with-balance)))
                                                       network-details))
                                        network)]
     {:db (cond-> db
            network      (update-in db-path/send assoc :network network)
            token-symbol (update-in db-path/send assoc :token token)
            bridge-tx?   (update-in db-path/send assoc :to-address address))
      :fx (if (or no-tx-type? (some? network) collectible-tx? (not asset-selected?))
            [[:dispatch [:wallet/switch-current-viewing-account address]]
             [:dispatch
              [:wallet/wizard-navigate-forward
               {:current-screen stack-id
                :start-flow?    (and start-flow? (not balance-in-only-one-network?))
                :flow-id        flow-id}]]]
            [[:dispatch [:dismiss-modal :screen/wallet.select-from]]
             [:dispatch [:wallet/switch-current-viewing-account address]]
             [:dispatch
              [:show-bottom-sheet
               {:content (fn []
                           [network-selection/view
                            {:token-symbol      (or token-symbol (:symbol token))
                             :source            :send
                             :on-select-network (fn [network]
                                                  (rf/dispatch [:hide-bottom-sheet])
                                                  (rf/dispatch
                                                   [:wallet/select-from-account
                                                    (assoc params :network network)]))}])}]]])})))

(rf/reg-event-fx
 :wallet/clean-route-data-for-collectible-tx
 (fn [{db :db}]
   (when (send-utils/tx-type-collectible? (-> db db/send :tx-type))
     {:db (update-in db
                     db-path/send
                     dissoc
                     :amount
                     :route
                     :suggested-routes
                     :last-request-uuid
                     :transaction-for-signing
                     :sign-transactions-callback-fx)
      :fx [[:dispatch [:wallet/stop-and-clean-suggested-routes]]]})))

(rf/reg-event-fx
 :wallet/collectible-amount-navigate-back
 (fn [{db :db} [{:keys []}]]
   (let [keep-tx-data? (#{:account-collectible-tab :screen/wallet-stack}
                        (-> db db/send :entry-point))]
     {:db (cond-> db
            :always             (update-in db-path/send dissoc :amount :route)
            (not keep-tx-data?) (update-in db-path/send dissoc :tx-type))
      :fx [[:dispatch [:navigate-back]]]})))

(rf/reg-event-fx
 :wallet.send/set-max-base-fee
 (fn [{db :db} [value]]
   {:db (assoc-in db (conj db-path/send :user-tx-settings :max-base-fee) value)}))

(rf/reg-event-fx
 :wallet.send/set-priority-fee
 (fn [{db :db} [value]]
   {:db (assoc-in db (conj db-path/send :user-tx-settings :priority-fee) value)}))

(rf/reg-event-fx
 :wallet.send/set-max-gas-amount
 (fn [{db :db} [value]]
   {:db (assoc-in db (conj db-path/send :user-tx-settings :gas-amount) value)}))

(rf/reg-event-fx
 :wallet.send/set-nonce
 (fn [{db :db} [value]]
   {:db (assoc-in db (conj db-path/send :user-tx-settings :nonce) value)}))

(defn set-fee-mode-effect
  [path-tx-identity gas-rate]
  (let [params [path-tx-identity gas-rate]]
    [:json-rpc/call
     [{:method   "wallet_setFeeMode"
       :params   params
       :on-error (fn [error]
                   (log/error "failed to set quick transaction settings"
                              {:event  :wallet.send/quick-fee-mode-confirmed
                               :error  (:message error)
                               :params params}))}]]))


(rf/reg-event-fx :wallet.send/quick-fee-mode-confirmed
 (fn [{db :db} [fee-mode]]
   (let [gas-rate                (transaction-settings/tx-fee-mode->gas-rate fee-mode)
         {:keys [tx-type route]} (get-in db db-path/send)
         path                    (first route)
         ;; bridge consist from 2 transactions - approval and send, so we need to apply
         ;; setting to both of them by making 2 calls
         set-fee-effects         (if (= tx-type :tx/bridge)
                                   [(set-fee-mode-effect (send-utils/path-identity path true) gas-rate)
                                    (set-fee-mode-effect (send-utils/path-identity path false)
                                                         gas-rate)]
                                   [(set-fee-mode-effect (send-utils/path-identity path) gas-rate)])]
     {:db (update-in db db-path/send assoc :user-fee-mode fee-mode)
      :fx (conj set-fee-effects
                [:dispatch [:wallet.send/mark-user-tx-settings-for-deletion]])})))

(defn set-custom-tx-effect
  [path-tx-identity custom-tx-params]
  (let [params [path-tx-identity custom-tx-params]]
    [:json-rpc/call
     [{:method   "wallet_setCustomTxDetails"
       :params   params
       :on-error (fn [error]
                   (log/error "failed to set custom tx settings"
                              {:event  :wallet.send/custom-transaction-settings-confirmed
                               :error  (:message error)
                               :params params}))}]]))

(rf/reg-event-fx :wallet.send/custom-transaction-settings-confirmed
 (fn [{db :db}]
   (let [{:keys [tx-type route user-tx-settings]} (get-in db db-path/send)
         path                                     (first route)
         custom-tx-params                         (send-utils/path-tx-custom-params user-tx-settings
                                                                                    path)
         ;; bridge consist from 2 transactions - approval and send, so we need to apply
         ;; setting to both of them by making 2 calls
         custom-tx-details-effects                (if (= tx-type :tx/bridge)
                                                    [(set-custom-tx-effect
                                                      (send-utils/path-identity path true)
                                                      custom-tx-params)
                                                     (set-custom-tx-effect
                                                      (send-utils/path-identity path false)
                                                      custom-tx-params)]
                                                    [(set-custom-tx-effect (send-utils/path-identity
                                                                            path)
                                                                           custom-tx-params)])]
     {:db (update-in db db-path/send assoc :user-fee-mode :tx-fee-mode/custom)
      :fx (conj custom-tx-details-effects
                [:dispatch [:wallet.send/mark-user-tx-settings-for-deletion]])})))

;; There is a delay between the moment when user selected
;; custom settings and the moment when new route arrived
;; with those settings applied. During this delay
;; we should keep user settings for ui. After new route
;; arrived we should clean the settings.

(rf/reg-event-fx :wallet.send/mark-user-tx-settings-for-deletion
 (fn [{db :db}]
   {:db (assoc-in db (conj db-path/send :user-tx-settings :delete-on-routes-update?) true)}))


(rf/reg-event-fx :wallet.send/set-sign-transactions-callback-fx
 (fn [{:keys [db]} [callback-fx]]
   {:db (assoc-in db (conj db-path/send :sign-transactions-callback-fx) callback-fx)}))

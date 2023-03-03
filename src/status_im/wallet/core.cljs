(ns status-im.wallet.core
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [status-im.async-storage.core :as async-storage]
    [status-im2.common.bottom-sheet.events :as bottom-sheet]
    [status-im.contact.db :as contact.db]
    [status-im.ethereum.core :as ethereum]
    [status-im.ethereum.eip55 :as eip55]
    [status-im.ethereum.ens :as ens]
    [status-im.ethereum.stateofus :as stateofus]
    [status-im.ethereum.tokens :as tokens]
    [utils.i18n :as i18n]
    [status-im.multiaccounts.update.core :as multiaccounts.update]
    [status-im.popover.core :as popover.core]
    [status-im.qr-scanner.core :as qr-scaner]
    [status-im.signing.eip1559 :as eip1559]
    [status-im.signing.gas :as signing.gas]
    [status-im2.config :as config]
    [status-im.utils.core :as utils.core]
    [utils.re-frame :as rf]
    [utils.datetime :as datetime]
    [status-im.utils.mobile-sync :as mobile-network-utils]
    [status-im.utils.money :as money]
    [status-im.utils.utils :as utils.utils]
    [status-im.wallet.db :as wallet.db]
    [status-im.wallet.prices :as prices]
    status-im.wallet.recipient.core
    [status-im.wallet.utils :as wallet.utils]
    [status-im2.common.json-rpc.events :as json-rpc]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]))

(defn get-balance
  [{:keys [address on-success on-error]}]
  (json-rpc/call
   {:method            "eth_getBalance"
    :params            [address "latest"]
    :on-success        #(on-success (money/bignumber %))
    :number-of-retries 50
    :on-error          on-error}))

(re-frame/reg-fx
 :wallet/get-balances
 (fn [addresses]
   (doseq [address addresses]
     (get-balance
      {:address    address
       :on-success #(re-frame/dispatch [::update-balance-success address %])
       :on-error   #(re-frame/dispatch [::update-balance-fail %])}))))

(defn assoc-error-message
  [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or err :unknown-error)))

(re-frame/reg-fx
 :wallet/get-cached-balances
 (fn [{:keys [addresses on-success on-error]}]
   (json-rpc/call
    {:method     "wallet_getCachedBalances"
     :params     [addresses]
     :on-success on-success
     :on-error   on-error})))

(rf/defn get-cached-balances
  [{:keys [db]} scan-all-tokens?]
  (let [addresses (map (comp string/lower-case :address)
                       (get db :multiaccount/accounts))]
    {:wallet/get-cached-balances
     {:addresses  addresses
      :on-success #(re-frame/dispatch [::set-cached-balances addresses % scan-all-tokens?])
      :on-error   #(re-frame/dispatch [::on-get-cached-balance-fail % scan-all-tokens?])}}))

(rf/defn on-update-balance-fail
  {:events [::update-balance-fail]}
  [{:keys [db]} err]
  (log/debug "Unable to get balance: " err)
  {:db (assoc-error-message db :balance-update :error-unable-to-get-balance)})

(rf/defn on-update-token-balance-fail
  {:events [::update-token-balance-fail]}
  [{:keys [db]} err]
  (log/debug "on-update-token-balance-fail: " err)
  {:db (assoc-error-message db :balance-update :error-unable-to-get-token-balance)})

(rf/defn open-transaction-details
  {:events [:wallet.ui/show-transaction-details]}
  [cofx hash address]
  (navigation/navigate-to-cofx cofx :wallet-transaction-details {:hash hash :address address}))

(defn dups
  [seq]
  (for [[id freq] (frequencies seq)
        :when     (> freq 1)]
    id))

(defn- clean-up-results
  "remove empty balances
   if there is no visible assets, returns all positive balances
   otherwise return only the visible assets balances"
  [results tokens assets]
  (let [balances
        (reduce
         (fn [acc [address balances]]
           (let [pos-balances
                 (reduce (fn [acc [token-address token-balance]]
                           (let [token-symbol (or (get tokens (name token-address))
                                                  (get tokens
                                                       (eip55/address->checksum (name token-address))))]
                             (if (or (and (empty? assets) (pos? token-balance))
                                     (and (seq assets) (assets token-symbol)))
                               (assoc acc token-symbol token-balance)
                               acc)))
                         {}
                         balances)]
             (if (not-empty pos-balances)
               (assoc acc (eip55/address->checksum (name address)) pos-balances)
               acc)))
         {}
         results)]
    (when (not-empty balances)
      balances)))

(defn get-token-balances
  [{:keys [addresses tokens scan-all-tokens? assets]}]
  (json-rpc/call
   {:method "wallet_getTokensBalances"
    :params [addresses (keys tokens)]
    :number-of-retries 50
    :on-success
    (fn [results]
      (when-let [balances (clean-up-results
                           results
                           tokens
                           (if scan-all-tokens? nil assets))]
        (re-frame/dispatch (if scan-all-tokens?
                             ;; NOTE: when there it is not a visible
                             ;; assets we make an initialization round
                             [::tokens-found balances]
                             [::update-tokens-balances-success balances]))))
    :on-error
    #(re-frame/dispatch [::update-token-balance-fail %])}))

(re-frame/reg-fx
 :wallet/get-tokens-balances
 get-token-balances)

(rf/defn collectibles-collection-fetch-success
  {:events [::collectibles-collection-fetch-success]}
  [{:keys [db]} address collection]
  {:db (assoc-in db [:wallet/collectible-collections address] collection)})

(rf/defn fetch-collectibles-collection
  {:events [::fetch-collectibles-collection]}
  [{:keys [db]}]
  (let [addresses (map (comp string/lower-case :address)
                       (get db :multiaccount/accounts))
        chain-id  (-> db
                      ethereum/current-network
                      ethereum/network->chain-id)]
    (when (get-in db [:multiaccount :opensea-enabled?])
      {:json-rpc/call (map
                       (fn [address]
                         {:method     "wallet_getOpenseaCollectionsByOwner"
                          :params     [chain-id address]
                          :on-error   (fn [error]
                                        (log/error "Unable to get Opensea collections" address error))
                          :on-success #(re-frame/dispatch [::collectibles-collection-fetch-success
                                                           address %])})
                       addresses)})))

(rf/defn collectible-assets-fetch-success
  {:events [::collectible-assets-fetch-success]}
  [{:keys [db]} address collectible-slug assets]
  {:db (-> db
           (assoc-in [:wallet/fetching-collection-assets collectible-slug] false)
           (assoc-in [:wallet/collectible-assets address collectible-slug] assets))})

(rf/defn collectibles-assets-fetch-error
  {:events [::collectibles-assets-fetch-error]}
  [{:keys [db]} collectible-slug]
  {:db (assoc-in db [:wallet/fetching-collection-assets collectible-slug] false)})

(rf/defn fetch-collectible-assets-by-owner-and-collection
  {:events [::fetch-collectible-assets-by-owner-and-collection]}
  [{:keys [db]} address collectible-slug limit]
  (let [chain-id (ethereum/network->chain-id (ethereum/current-network db))]
    {:db            (assoc-in db [:wallet/fetching-collection-assets collectible-slug] true)
     :json-rpc/call [{:method     "wallet_getOpenseaAssetsByOwnerAndCollection"
                      :params     [chain-id address collectible-slug limit]
                      :on-error   (fn [error]
                                    (log/error "Unable to get collectible assets" address error)
                                    (re-frame/dispatch [::collectibles-assets-fetch-error
                                                        collectible-slug]))
                      :on-success #(re-frame/dispatch [::collectible-assets-fetch-success address
                                                       collectible-slug %])}]}))

(rf/defn show-nft-details
  {:events [::show-nft-details]}
  [cofx asset]
  (rf/merge cofx
            {:db (assoc (:db cofx) :wallet/selected-collectible asset)}
            (navigation/open-modal :nft-details {})))

(defn rpc->token
  [tokens]
  (reduce (fn [acc {:keys [address] :as token}]
            (assoc acc
                   address
                   (assoc token :custom? true)))
          {}
          tokens))

(rf/defn initialize-tokens
  [{:keys [db]} tokens custom-tokens]
  (let [default-tokens (utils.core/index-by :address tokens)
        ;;we want to override custom-tokens by default
        all-tokens     (merge (rpc->token custom-tokens) default-tokens)]
    {:db (assoc db :wallet/all-tokens all-tokens)}))

(rf/defn initialize-favourites
  [{:keys [db]} favourites]
  {:db (assoc db
              :wallet/favourites
              (reduce (fn [acc {:keys [address] :as favourit}]
                        (assoc acc address favourit))
                      {}
                      favourites))})

(rf/defn update-balances
  {:events [:wallet/update-balances]}
  [{{:keys [network-status :wallet/all-tokens
            multiaccount :multiaccount/accounts]
     :as   db}
    :db
    :as cofx} addresses scan-all-tokens?]
  (log/debug "update-balances"
             "accounts"         addresses
             "scan-all-tokens?" scan-all-tokens?)
  (let [addresses                        (or addresses (map (comp string/lower-case :address) accounts))
        {:keys [:wallet/visible-tokens]} multiaccount
        chain                            (ethereum/chain-keyword db)
        assets                           (get visible-tokens chain)
        tokens                           (->> (vals all-tokens)
                                              (remove #(or (:hidden? %)
                                                           ;;if not scan-all-tokens? remove not visible
                                                           ;;tokens
                                                           (and (not scan-all-tokens?)
                                                                (not (get assets (:symbol %))))))
                                              (reduce (fn [acc {:keys [address symbol]}]
                                                        (assoc acc address symbol))
                                                      {}))]
    (when (and (seq addresses)
               (not= network-status :offline))
      (rf/merge
       cofx
       {:wallet/get-balances        addresses
        :wallet/get-tokens-balances {:addresses        addresses
                                     :tokens           tokens
                                     :assets           assets
                                     :scan-all-tokens? scan-all-tokens?}
        :db                         (prices/clear-error-message db :balance-update)}
       (when-not assets
         (multiaccounts.update/multiaccount-update
          :wallet/visible-tokens
          (assoc visible-tokens
                 chain
                 (or (config/default-visible-tokens chain)
                     #{}))
          {}))))))

(rf/defn on-get-cached-balance-fail
  {:events [::on-get-cached-balance-fail]}
  [{:keys [db] :as cofx} err scan-all-tokens?]
  (log/warn "Can't fetch cached balances" err)
  (update-balances cofx nil scan-all-tokens?))

(defn- set-checked
  [tokens-id token-id checked?]
  (let [tokens-id (or tokens-id #{})]
    (if checked?
      (conj tokens-id token-id)
      (disj tokens-id token-id))))

(rf/defn update-balance
  {:events [::update-balance-success]}
  [{:keys [db]} address balance]
  {:db (assoc-in db
        [:wallet :accounts (eip55/address->checksum address) :balance :ETH]
        (money/bignumber balance))})

(rf/defn set-cached-balances
  {:events [::set-cached-balances]}
  [cofx addresses balances scan-all-tokens?]
  (apply rf/merge
         cofx
         (update-balances nil scan-all-tokens?)
         (map (fn [{:keys [address balance]}]
                (update-balance address balance))
              balances)))

(defn has-empty-balances?
  [db]
  (some #(nil? (get-in % [:balance :ETH]))
        (get-in db [:wallet :accounts])))

(rf/defn update-toggle-in-settings
  [{{:keys [multiaccount] :as db} :db :as cofx} symbol checked?]
  (let [chain          (ethereum/chain-keyword db)
        visible-tokens (get multiaccount :wallet/visible-tokens)]
    (rf/merge cofx
              (multiaccounts.update/multiaccount-update
               :wallet/visible-tokens
               (update visible-tokens
                       chain
                       #(set-checked % symbol checked?))
               {})
              #(when checked?
                 (update-balances % nil nil)))))

(rf/defn toggle-visible-token
  {:events [:wallet.settings/toggle-visible-token]}
  [cofx symbol checked?]
  (update-toggle-in-settings cofx symbol checked?))

(rf/defn update-tokens-balances
  {:events [::update-tokens-balances-success]}
  [{:keys [db]} balances]
  (let [accounts (get-in db [:wallet :accounts])]
    {:db (assoc-in db
          [:wallet :accounts]
          (reduce (fn [acc [address balances]]
                    (assoc-in acc
                     [address :balance]
                     (reduce (fn [acc [token-symbol balance]]
                               (assoc acc
                                      token-symbol
                                      (money/bignumber balance)))
                             (get-in accounts [address :balance])
                             balances)))
                  accounts
                  balances))}))

(rf/defn set-zero-balances
  [cofx {:keys [address]}]
  (rf/merge
   cofx
   (update-balance address 0)
   (update-tokens-balances {address {:SNT 0}})))

(rf/defn configure-token-balance-and-visibility
  {:events [::tokens-found]}
  [{:keys [db] :as cofx} balances]
  (let [chain                (ethereum/chain-keyword db)
        visible-tokens       (get-in db [:multiaccount :wallet/visible-tokens])
        chain-visible-tokens (into (or (config/default-visible-tokens chain)
                                       #{})
                                   (flatten (map keys (vals balances))))]
    (rf/merge cofx
              (multiaccounts.update/multiaccount-update
               :wallet/visible-tokens
               (update visible-tokens chain set/union chain-visible-tokens)
               {})
              (update-tokens-balances balances)
              (prices/update-prices))))

(rf/defn add-custom-token
  [cofx {:keys [symbol]}]
  (update-toggle-in-settings cofx symbol true))

(rf/defn remove-custom-token
  [cofx {:keys [symbol]}]
  (update-toggle-in-settings cofx symbol false))

(rf/defn set-and-validate-amount
  {:events [:wallet.send/set-amount-text]}
  [{:keys [db]} amount]
  {:db (assoc-in db [:wallet/prepare-transaction :amount-text] amount)})

(rf/defn wallet-send-gas-price-success
  {:events [:wallet.send/update-gas-price-success]}
  [{db :db} tx-entry price {:keys [maxFeePerGas maxPriorityFeePerGas gasPrice]}]
  (if (eip1559/sync-enabled?)
    (let [{:keys [slow-base-fee normal-base-fee fast-base-fee
                  current-base-fee max-priority-fee]}
          price
          max-priority-fee-bn (money/with-precision (signing.gas/get-suggested-tip max-priority-fee) 0)
          fee-cap (-> normal-base-fee
                      money/bignumber
                      (money/add max-priority-fee-bn)
                      money/to-hex)
          tip-cap (money/to-hex max-priority-fee-bn)]
      {:db (-> db
               (update tx-entry
                       assoc
                       :maxFeePerGas         (or maxFeePerGas fee-cap)
                       :maxPriorityFeePerGas (or maxPriorityFeePerGas tip-cap))
               (assoc :wallet/current-base-fee     current-base-fee
                      :wallet/normal-base-fee      normal-base-fee
                      :wallet/slow-base-fee        slow-base-fee
                      :wallet/fast-base-fee        fast-base-fee
                      :wallet/current-priority-fee max-priority-fee)
               (assoc-in [:signing/edit-fee :gas-price-loading?] false))})
    {:db (-> db
             (assoc-in [:wallet/prepare-transaction :gasPrice] (or gasPrice price))
             (assoc-in [:signing/edit-fee :gas-price-loading?] false))}))

(rf/defn set-max-amount
  {:events [:wallet.send/set-max-amount]}
  [{:keys [db]} {:keys [amount decimals symbol]}]
  (let [^js gas      (money/bignumber 21000)
        ^js gasPrice (or
                      (get-in db [:wallet/prepare-transaction :maxFeePerGas])
                      (get-in db [:wallet/prepare-transaction :gasPrice]))
        ^js fee      (when gasPrice (.times gas gasPrice))
        amount-text  (if (= :ETH symbol)
                       (when (and fee (money/sufficient-funds? fee amount))
                         (str (wallet.utils/format-amount (.minus amount fee) decimals)))
                       (str (wallet.utils/format-amount amount decimals)))]
    (when amount-text
      {:db (cond-> db
             :always
             (assoc-in [:wallet/prepare-transaction :amount-text] amount-text)
             (= :ETH symbol)
             (assoc-in [:wallet/prepare-transaction :gas] gas))})))

(rf/defn set-and-validate-request-amount
  {:events [:wallet.request/set-amount-text]}
  [{:keys [db]} amount]
  {:db (assoc-in db [:wallet/prepare-transaction :amount-text] amount)})

(rf/defn request-transaction-button-clicked-from-chat
  {:events [:wallet.ui/request-transaction-button-clicked]}
  [{:keys [db] :as cofx} {:keys [to amount from token]}]
  (let [{:keys [symbol address]} token
        from-address             (:address from)
        identity                 (:current-chat-id db)]
    (rf/merge cofx
              {:db            (dissoc db :wallet/prepare-transaction)
               :json-rpc/call [{:method      "wakuext_requestTransaction"
                                :params      [(:public-key to)
                                              amount
                                              (when-not (= symbol :ETH)
                                                address)
                                              from-address]
                                :js-response true
                                :on-success  #(re-frame/dispatch [:transport/message-sent %])}]})))

(rf/defn accept-request-transaction-button-clicked-from-command
  {:events [:wallet.ui/accept-request-transaction-button-clicked-from-command]}
  [{:keys [db]} chat-id {:keys [value contract] :as request-parameters}]
  (let [identity (:current-chat-id db)
        all-tokens (:wallet/all-tokens db)
        {:keys [symbol decimals]}
        (if (seq contract)
          (get all-tokens contract)
          (tokens/native-currency (ethereum/get-current-network db)))
        amount-text (str (money/internal->formatted value symbol decimals))]
    {:db       (assoc db
                      :wallet/prepare-transaction
                      {:from               (ethereum/get-default-account (:multiaccount/accounts db))
                       :to                 (or (get-in db [:contacts/contacts identity])
                                               (-> identity
                                                   contact.db/public-key->new-contact
                                                   contact.db/enrich-contact))
                       :request-parameters request-parameters
                       :chat-id            chat-id
                       :symbol             symbol
                       :amount-text        amount-text
                       :request?           true
                       :from-chat?         true})
     :dispatch [:open-modal :prepare-send-transaction]}))

(rf/defn set-and-validate-amount-request
  {:events [:wallet.request/set-and-validate-amount]}
  [{:keys [db]} amount symbol decimals]
  (let [{:keys [value error]} (wallet.db/parse-amount amount decimals)]
    {:db (-> db
             (assoc-in [:wallet :request-transaction :amount]
                       (money/formatted->internal value symbol decimals))
             (assoc-in [:wallet :request-transaction :amount-text] amount)
             (assoc-in [:wallet :request-transaction :amount-error] error))}))

(rf/defn set-symbol-request
  {:events [:wallet.request/set-symbol]}
  [{:keys [db]} symbol]
  {:db (assoc-in db [:wallet :request-transaction :symbol] symbol)})

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [chain-id ens-name cb]}]
   (ens/address chain-id ens-name cb)))

(rf/defn on-recipient-address-resolved
  {:events [::recipient-address-resolved]}
  [{:keys [db]} address]
  {:db                       (assoc-in db [:wallet/prepare-transaction :to :address] address)
   :signing/update-gas-price {:success-callback
                              #(re-frame/dispatch
                                [:wallet.send/update-gas-price-success :wallet/prepare-transaction %])
                              :network-id (get-in (ethereum/current-network db)
                                                  [:config :NetworkId])}})

(rf/defn prepare-transaction-from-chat
  {:events [:wallet/prepare-transaction-from-chat]}
  [{:keys [db]}]
  (let [identity (:current-chat-id db)
        {:keys [ens-verified name] :as contact}
        (or (get-in db [:contacts/contacts identity])
            (-> identity
                contact.db/public-key->new-contact
                contact.db/enrich-contact))]
    (cond-> {:db       (assoc db
                              :wallet/prepare-transaction
                              {:from       (ethereum/get-default-account
                                            (:multiaccount/accounts db))
                               :to         contact
                               :symbol     :ETH
                               :from-chat? true})
             :dispatch [:open-modal :prepare-send-transaction]}
      ens-verified
      (assoc ::resolve-address
             {:chain-id (ethereum/chain-id db)
              :ens-name (if (= (.indexOf ^js name ".") -1)
                          (stateofus/subdomain name)
                          name)
              ;;TODO handle errors and timeout for ens name resolution
              :cb       #(re-frame/dispatch [::recipient-address-resolved %])}))))

(rf/defn prepare-request-transaction-from-chat
  {:events [:wallet/prepare-request-transaction-from-chat]}
  [{:keys [db]}]
  (let [identity (:current-chat-id db)]
    {:db       (assoc db
                      :wallet/prepare-transaction
                      {:from             (ethereum/get-default-account (:multiaccount/accounts db))
                       :to               (or (get-in db [:contacts/contacts identity])
                                             (-> identity
                                                 contact.db/public-key->new-contact
                                                 contact.db/enrich-contact))
                       :symbol           :ETH
                       :from-chat?       true
                       :request-command? true})
     :dispatch [:open-modal :request-transaction]}))

(rf/defn prepare-transaction-from-wallet
  {:events [:wallet/prepare-transaction-from-wallet]}
  [{:keys [db]} account]
  {:db                       (assoc db
                                    :wallet/prepare-transaction
                                    {:from       account
                                     :to         nil
                                     :symbol     :ETH
                                     :from-chat? false})
   :dispatch                 [:open-modal :prepare-send-transaction]
   :signing/update-gas-price {:success-callback
                              #(re-frame/dispatch
                                [:wallet.send/update-gas-price-success :wallet/prepare-transaction %])
                              :network-id (get-in (ethereum/current-network db)
                                                  [:config :NetworkId])}})

(rf/defn cancel-transaction-command
  {:events [:wallet/cancel-transaction-command]}
  [{:keys [db]}]
  (let [identity (:current-chat-id db)]
    {:db (dissoc db :wallet/prepare-transaction)}))

(rf/defn finalize-transaction-from-command
  {:events [:wallet/finalize-transaction-from-command]}
  [{:keys [db]} account to symbol amount]
  {:db (assoc db
              :wallet/prepare-transaction
              {:from          account
               :to            to
               :symbol        symbol
               :amount        amount
               :from-command? true})})

(rf/defn view-only-qr-scanner-allowed
  {:events [:wallet.add-new/qr-scanner]}
  [{:keys [db] :as cofx} options]
  (rf/merge cofx
            {:db (update-in db [:add-account] dissoc :address)}
            (qr-scaner/scan-qr-code options)))

(rf/defn wallet-send-set-symbol
  {:events [:wallet.send/set-symbol]}
  [{:keys [db] :as cofx} symbol]
  (rf/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction :symbol] symbol)}
            (bottom-sheet/hide-bottom-sheet)))

(rf/defn wallet-send-set-field
  {:events [:wallet.send/set-field]}
  [{:keys [db] :as cofx} field value]
  (rf/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction field] value)}
            (bottom-sheet/hide-bottom-sheet)))

(rf/defn wallet-request-set-field
  {:events [:wallet.request/set-field]}
  [{:keys [db] :as cofx} field value]
  (rf/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction field] value)}
            (bottom-sheet/hide-bottom-sheet)))

(rf/defn navigate-to-recipient-code
  {:events [:wallet.send/navigate-to-recipient-code]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (assoc :wallet/recipient {}))}
            (bottom-sheet/hide-bottom-sheet)
            (navigation/open-modal :recipient nil)))

(rf/defn show-delete-account-confirmation
  {:events [:wallet.settings/show-delete-account-confirmation]}
  [_ account]
  {:ui/show-confirmation {:title               (i18n/label :t/are-you-sure?)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [:wallet.accounts/delete-account
                                                                    account])
                          :on-cancel           #()}})

(re-frame/reg-fx
 ::check-recent-history
 (fn [addresses]
   (log/info "[wallet] check recent history" addresses)
   (json-rpc/call
    {:method     "wallet_checkRecentHistory"
     :params     [addresses]
     :on-success #(log/info "[wallet] wallet_checkRecentHistory success")
     :on-error   #(log/error "[wallet] wallet_checkRecentHistory error" %)})))

(def ms-2-min (datetime/minutes 2))
(def ms-3-min (datetime/minutes 3))
(def ms-5-min (datetime/minutes 5))
(def ms-10-min (datetime/minutes 10))
(def ms-20-min (datetime/minutes 20))

(def custom-intervals
  {:ms-2-min  ms-2-min
   :ms-3-min  ms-3-min
   :ms-5-min  ms-5-min
   :ms-10-min ms-10-min
   :ms-20-min ms-20-min})

(def next-custom-interval
  {:ms-2-min :ms-3-min
   :ms-3-min :ms-5-min
   :ms-5-min :ms-10-min})

(defn get-next-custom-interval
  [{:keys [:wallet-service/custom-interval]}]
  (get next-custom-interval custom-interval))

(defn get-max-block-with-transfer
  [db]
  (reduce
   (fn [block [_ {:keys [max-block]}]]
     (if (or (nil? block)
             (> max-block block))
       max-block
       block))
   nil
   (get-in db [:wallet :accounts])))

(defn get-restart-interval
  [db]
  (let [max-block       (get-max-block-with-transfer db)
        custom-interval (get db :wallet-service/custom-interval)]
    (cond
      (ethereum/custom-rpc-node?
       (ethereum/current-network db))
      ms-2-min

      (and max-block
           (zero? max-block)
           (nil? (get db :wallet/keep-watching-until-ms)))
      (log/info "[wallet] No transactions found")

      :else
      (get custom-intervals custom-interval ms-20-min))))

(defn get-watching-interval
  [db]
  (if (ethereum/custom-rpc-node?
       (ethereum/current-network db))
    ms-2-min
    ms-10-min))

(rf/defn after-checking-history
  [{:keys [db] :as cofx}]
  (log/info "[wallet] after-checking-history")
  {:db (dissoc db
        :wallet/recent-history-fetching-started?
        :wallet/refreshing-history?)})

(defn set-timeout
  [db]
  (when-let [interval (get-restart-interval db)]
    (utils.utils/set-timeout
     #(re-frame.core/dispatch [::restart])
     interval)))

(rf/defn check-recent-history
  [{:keys [db] :as cofx}
   {:keys [on-recent-history-fetching force-restart?]}]
  (let [addresses   (map :address (get db :multiaccount/accounts))
        old-timeout (get db :wallet-service/restart-timeout)
        timeout     (if force-restart?
                      old-timeout
                      (set-timeout db))]
    {:db (-> db
             (assoc :wallet-service/restart-timeout timeout
                    :wallet-service/custom-interval (get-next-custom-interval
                                                     db)
                    :wallet/was-started? true
                    :wallet/on-recent-history-fetching
                    on-recent-history-fetching))
     ::check-recent-history addresses
     ::utils.utils/clear-timeouts
     [(when (not= timeout old-timeout) old-timeout)]}))

(rf/defn restart-wallet-service
  [{:keys [db] :as cofx}
   {:keys [force-restart? on-recent-history-fetching]
    :as   params}]
  (when (:multiaccount db)
    (let [syncing-allowed? (mobile-network-utils/syncing-allowed? cofx)
          binance-chain?   (ethereum/binance-chain? db)]
      (log/info "restart-wallet-service"
                "force-restart?"   force-restart?
                "syncing-allowed?" syncing-allowed?
                "binance-chain?"   binance-chain?)
      (if (and (or syncing-allowed?
                   force-restart?)
               (not binance-chain?))
        (check-recent-history cofx params)
        (after-checking-history cofx)))))

(def background-cooldown-time (datetime/minutes 3))

(rf/defn restart-wallet-service-after-background
  [{:keys [now db] :as cofx} background-time]
  (when (and (get db :wallet/was-started?)
             (> (- now background-time)
                background-cooldown-time))
    (restart-wallet-service cofx nil)))

(rf/defn restart
  {:events [::restart]}
  [{:keys [db] :as cofx} force-restart?]
  (restart-wallet-service
   cofx
   {:force-restart? force-restart?}))

(re-frame/reg-fx
 :load-transaction-by-hash
 (fn [[address hash]]
   (log/info "calling wallet_loadTransferByHash" address hash)
   (json-rpc/call
    {:method     "wallet_loadTransferByHash"
     :params     [address hash]
     :on-success #(re-frame/dispatch [:transaction/get-fetched-transfers])
     :on-error   #(log/warn "Transfer loading failed" %)})))

(rf/defn load-transaction-by-hash
  [_ address hash]
  {:load-transaction-by-hash [address hash]})

(rf/defn transaction-included
  {:events [::transaction-included]}
  [{:keys [db] :as cofx} address hash]
  (if (ethereum/binance-chain? db)
    (load-transaction-by-hash cofx address hash)
    (restart cofx true)))

(def pull-to-refresh-cooldown-period (* 1 60 1000))

(rf/defn restart-on-pull
  {:events [:wallet.ui/pull-to-refresh-history]}
  [{:keys [db now] :as cofx}]
  (let [last-pull         (get db :wallet/last-pull-time)
        fetching-history? (get db :wallet/recent-history-fetching-started?)]
    (when (and (not fetching-history?)
               (or (not last-pull)
                   (> (- now last-pull) pull-to-refresh-cooldown-period)))
      (rf/merge
       {:db (assoc db
                   :wallet/last-pull-time      now
                   :wallet/refreshing-history? true)}
       (restart-wallet-service
        {:force-restart? true})))))

(re-frame/reg-fx
 ::start-watching
 (fn [hashes]
   (log/info "[wallet] watch transactions" hashes)
   (doseq [[address hash] hashes]
     (json-rpc/call
      {:method     "wallet_watchTransaction"
       :params     [hash]
       :on-success #(re-frame.core/dispatch [::transaction-included address hash])
       :on-error   #(log/info "[wallet] watch transaction error" % "hash" hash)}))))

(rf/defn watch-tx
  {:events [:watch-tx]}
  [{:keys [db] :as cofx} address tx-id]
  {::start-watching [[address tx-id]]})

(rf/defn watch-transsactions
  [_ hashes]
  {::start-watching hashes})

(rf/defn clear-timeouts
  [{:keys [db]}]
  (log/info "[wallet] clear-timeouts")
  (let [restart-timeout-id (get db :wallet-service/restart-timeout)]
    {:db                          (dissoc db :wallet-service/restart-timeout)
     ::utils.utils/clear-timeouts [restart-timeout-id]}))

(rf/defn get-buy-crypto-preference
  {:events [::get-buy-crypto]}
  [_]
  {::async-storage/get {:keys [:buy-crypto-hidden]
                        :cb   #(re-frame/dispatch [::store-buy-crypto-preference %])}})

(rf/defn wallet-will-focus
  {:events [::wallet-stack]}
  [{:keys [db]}]
  (let [wallet-set-up-passed? (get-in db [:multiaccount :wallet-set-up-passed?])
        sign-phrase-showed?   (get db :wallet/sign-phrase-showed?)]
    {:dispatch-n [[:wallet.ui/pull-to-refresh]] ;TODO temporary simple fix for v1
     ;;[:show-popover {:view [signing-phrase/signing-phrase]}]]
     :db         (if (or wallet-set-up-passed? sign-phrase-showed?)
                   db
                   (assoc db :wallet/sign-phrase-showed? true))}))

(rf/defn wallet-wallet-add-custom-token
  {:events [:wallet/wallet-add-custom-token]}
  [{:keys [db]}]
  {:db (dissoc db :wallet/custom-token-screen)})

(rf/defn hide-buy-crypto
  {:events [::hide-buy-crypto]}
  [{:keys [db]}]
  {:db                  (assoc db :wallet/buy-crypto-hidden true)
   ::async-storage/set! {:buy-crypto-hidden true}})

(rf/defn store-buy-crypto
  {:events [::store-buy-crypto-preference]}
  [{:keys [db]} {:keys [buy-crypto-hidden]}]
  {:db (assoc db :wallet/buy-crypto-hidden buy-crypto-hidden)})

(rf/defn contract-address-paste
  {:events [:wallet.custom-token.ui/contract-address-paste]}
  [_]
  {:wallet.custom-token/contract-address-paste nil})

(rf/defn transactions-add-filter
  {:events [:wallet.transactions/add-filter]}
  [{:keys [db]} id]
  {:db (update-in db [:wallet :filters] conj id)})

(rf/defn transactions-remove-filter
  {:events [:wallet.transactions/remove-filter]}
  [{:keys [db]} id]
  {:db (update-in db [:wallet :filters] disj id)})

(rf/defn transactions-add-all-filters
  {:events [:wallet.transactions/add-all-filters]}
  [{:keys [db]}]
  {:db (assoc-in db
        [:wallet :filters]
        wallet.db/default-wallet-filters)})

(rf/defn settings-navigate-back-pressed
  {:events [:wallet.settings.ui/navigate-back-pressed]}
  [cofx on-close]
  (rf/merge cofx
            (when on-close
              {:dispatch on-close})
            (navigation/navigate-back)))

(rf/defn stop-fetching-on-empty-tx-history
  [{:keys [db now] :as cofx} transfers]
  (let [non-empty-history? (get db :wallet/non-empty-tx-history?)
        custom-node?       (ethereum/custom-rpc-node?
                            (ethereum/current-network db))
        until-ms           (get db :wallet/keep-watching-until-ms)]
    (when-not (and until-ms (> until-ms now))
      (rf/merge
       cofx
       {:db (dissoc db :wallet/keep-watching-until-ms)}
       (if (and (not non-empty-history?)
                (empty? transfers)
                (not custom-node?))
         (clear-timeouts)
         (fn [{:keys [db]}]
           {:db (assoc db :wallet/non-empty-tx-history? true)}))))))

(rf/defn keep-watching-history
  {:events [:wallet/keep-watching]}
  [{:keys [db now] :as cofx}]
  (let [non-empty-history? (get db :wallet/non-empty-tx-history?)
        old-timeout        (get db :wallet-service/restart-timeout)
        db                 (assoc db :wallet-service/custom-interval :ms-2-min)
        timeout            (set-timeout db)]
    {:db                          (assoc db
                                         :wallet/keep-watching-until-ms  (+ now (datetime/minutes 30))
                                         :wallet-service/restart-timeout timeout
                                         :wallet-service/custom-interval (get-next-custom-interval db))
     ::utils.utils/clear-timeouts [old-timeout]}))

(re-frame/reg-fx
 ::set-inital-range
 (fn []
   (json-rpc/call
    {:method            "wallet_setInitialBlocksRange"
     :params            []
     :number-of-retries 10
     :on-success        #(log/info "Initial blocks range was successfully set")
     :on-error          #(log/info "Initial blocks range was not set")})))

(rf/defn set-initial-blocks-range
  [{:keys [db]}]
  {:db                (assoc db :wallet/new-account true)
   ::set-inital-range nil})

(rf/defn tab-opened
  {:events [:wallet/tab-opened]}
  [{:keys [db] :as cofx}]
  (when-not (get db :wallet/was-started?)
    (restart-wallet-service cofx nil)))

(rf/defn set-max-block
  [{:keys [db]} address block]
  (log/debug "set-max-block"
             "address" address
             "block"   block)
  {:db (assoc-in db [:wallet :accounts address :max-block] block)})

(rf/defn set-max-block-with-transfers
  [{:keys [db] :as cofx} address transfers]
  (let [max-block (reduce
                   (fn [max-block {:keys [block]}]
                     (if (> block max-block)
                       block
                       max-block))
                   (get-in db [:wallet :accounts address :max-block] 0)
                   transfers)]
    (set-max-block cofx address max-block)))

(rf/defn share
  {:events [:wallet/share-popover]}
  [{:keys [db] :as cofx} address]
  (let [non-empty-history? (get db :wallet/non-empty-tx-history?)
        restart?           (and (not (get db :wallet/non-empty-tx-history?))
                                (not (get db :wallet-service/restart-timeout)))]
    (rf/merge
     cofx
     (popover.core/show-popover
      {:view    :share-account
       :address address})
     (keep-watching-history))))

(re-frame/reg-fx
 ::get-pending-transactions
 (fn []
   (json-rpc/call
    {:method     "wallet_getPendingTransactions"
     :params     []
     :on-success #(re-frame/dispatch [:wallet/on-retreiving-pending-transactions %])})))

(rf/defn get-pending-transactions
  {:events [:wallet/get-pending-transactions]}
  [_]
  (log/info "[wallet] get pending transactions")
  {::get-pending-transactions nil})

(defn normalize-transaction
  [db {:keys [symbol gasPrice gasLimit value from to] :as transaction}]
  (let [symbol (keyword symbol)
        token  (tokens/symbol->token (:wallet/all-tokens db) symbol)]
    (-> transaction
        (select-keys [:timestamp :hash :data])
        (assoc :from      (eip55/address->checksum from)
               :to        (eip55/address->checksum to)
               :type      :pending
               :symbol    symbol
               :token     token
               :value     (money/bignumber value)
               :gas-price (money/bignumber gasPrice)
               :gas-limit (money/bignumber gasLimit)))))

(rf/defn on-retriving-pending-transactions
  {:events [:wallet/on-retreiving-pending-transactions]}
  [{:keys [db]} raw-transactions]
  (log/info "[wallet] pending transactions")
  {:db
   (reduce (fn [db {:keys [from hash] :as transaction}]
             (let [path [:wallet :accounts from :transactions hash]]
               (if-not (get-in db path)
                 (assoc-in db path transaction)
                 db)))
           db
           (map (partial normalize-transaction db) raw-transactions))
   ::start-watching (map (juxt :from :hash) raw-transactions)})

(re-frame/reg-fx
 :wallet/delete-pending-transactions
 (fn [hashes]
   (log/info "[wallet] delete pending transactions")
   (doseq [hash hashes]
     (json-rpc/call
      {:method     "wallet_deletePendingTransaction"
       :params     [hash]
       :on-success #(log/info "[wallet] pending transaction deleted" hash)}))))

(rf/defn switch-transactions-management-enabled
  {:events [:multiaccounts.ui/switch-transactions-management-enabled]}
  [{:keys [db]} enabled?]
  {::async-storage/set! {:transactions-management-enabled? enabled?}
   :db                  (assoc db :wallet/transactions-management-enabled? enabled?)})

(rf/defn update-current-block
  {:events [::update-current-block]}
  [{:keys [db]} block]
  {:db (assoc db :ethereum/current-block block)})

(re-frame/reg-fx
 ::request-current-block-update
 (fn []
   (json-rpc/call
    {:method     "eth_getBlockByNumber"
     :params     ["latest" false]
     :on-success #(re-frame/dispatch [::update-current-block (get % :number)])})))

(rf/defn request-current-block-update
  [_]
  {::request-current-block-update nil})

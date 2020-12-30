(ns status-im.wallet.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.utils.config :as config]
            [status-im.qr-scanner.core :as qr-scaner]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.utils.core :as utils.core]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils.utils]
            [taoensso.timbre :as log]
            [status-im.wallet.db :as wallet.db]
            [clojure.string :as string]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [status-im.wallet.prices :as prices]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.mobile-network-settings.utils :as mobile-network-utils]
            [status-im.utils.datetime :as datetime]
            status-im.wallet.recipient.core))

(defn get-balance
  [{:keys [address on-success on-error]}]
  (json-rpc/call
   {:method            "eth_getBalance"
    :params            [address "latest"]
    :on-success        on-success
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

(defn assoc-error-message [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or err :unknown-error)))

(fx/defn on-update-balance-fail
  {:events [::update-balance-fail]}
  [{:keys [db]} err]
  (log/debug "Unable to get balance: " err)
  {:db (assoc-error-message db :balance-update :error-unable-to-get-balance)})

(fx/defn on-update-token-balance-fail
  {:events [::update-token-balance-fail]}
  [{:keys [db]} err]
  (log/debug "Unable to get tokens balances: " err)
  {:db (assoc-error-message db :balance-update :error-unable-to-get-token-balance)})

(fx/defn open-transaction-details
  [cofx hash address]
  (navigation/navigate-to-cofx cofx :wallet-stack {:screen  :wallet-transaction-details
                                                   :initial false
                                                   :params  {:hash hash :address address}}))

(defn- validate-token-name!
  [{:keys [address symbol name]}]
  (json-rpc/eth-call
   {:contract address
    :method "name()"
    :outputs ["string"]
    :on-success
    (fn [[contract-name]]
      (when (and (seq contract-name)
                 (not= name contract-name))
        (let [message (i18n/label :t/token-auto-validate-name-error
                                  {:symbol   symbol
                                   :expected name
                                   :actual   contract-name
                                   :address  address})]
          (log/warn message)
          (utils.utils/show-popup (i18n/label :t/warning) message))))}))

(defn- validate-token-symbol!
  [{:keys [address symbol]}]
  (when-not (or (= symbol :DCN) (= symbol :SUPRR)) ;; ignore this symbol because it has weird symbol
    (json-rpc/eth-call
     {:contract address
      :method "symbol()"
      :outputs ["string"]
      :on-success
      (fn [[contract-symbol]]
        ;;NOTE(goranjovic): skipping check if field not set in contract
        (when (and (seq contract-symbol)
                   (not= (clojure.core/name symbol) contract-symbol))
          (let [message (i18n/label :t/token-auto-validate-symbol-error
                                    {:symbol   symbol
                                     :expected (clojure.core/name symbol)
                                     :actual   contract-symbol
                                     :address  address})]
            (log/warn message)
            (utils.utils/show-popup (i18n/label :t/warning) message))))})))

(defn- validate-token-decimals!
  [{:keys [address symbol decimals nft?]}]
  (when-not nft?
    (json-rpc/eth-call
     {:contract address
      :method "decimals()"
      :outputs ["uint256"]
      :on-success
      (fn [[contract-decimals]]
        (when (and (not (nil? contract-decimals))
                   (not= decimals contract-decimals))
          (let [message (i18n/label :t/token-auto-validate-decimals-error
                                    {:symbol   symbol
                                     :expected decimals
                                     :actual   contract-decimals
                                     :address  address})]
            (log/warn message)
            (utils.utils/show-popup (i18n/label :t/warning) message))))})))

(defn dups [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
    id))

(re-frame/reg-fx
 :wallet/validate-tokens
 (fn [[tokens all-default-tokens]]
   (let [symb-dups (dups (map :symbol all-default-tokens))
         addr-dups (dups (map :address all-default-tokens))]
     (when (seq symb-dups)
       (utils.utils/show-popup (i18n/label :t/warning) (str "Duplicated tokens symbols" symb-dups)))
     (when (seq addr-dups)
       (utils.utils/show-popup (i18n/label :t/warning) (str "Duplicated tokens addresses" addr-dups)))
     (doseq [token (vals tokens)]
       (validate-token-decimals! token)
       (validate-token-symbol! token)
       (validate-token-name! token)))))

(defn- clean-up-results
  "remove empty balances
   if there is no visible assets, returns all positive balances
   otherwise return only the visible assets balances"
  [results tokens assets]
  (let [balances
        (reduce (fn [acc [address balances]]
                  (let [pos-balances
                        (reduce (fn [acc [token-address token-balance]]
                                  (let [token-symbol (or (get tokens (name token-address))
                                                         (get tokens (eip55/address->checksum (name token-address))))]
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
  [{:keys [addresses tokens init? assets]}]
  (json-rpc/call
   {:method            "wallet_getTokensBalances"
    :params            [addresses (keys tokens)]
    :number-of-retries 50
    :on-success
    (fn [results]
      (when-let [balances (clean-up-results results tokens (if init? nil assets))]
        (re-frame/dispatch (if init?
                             ;; NOTE: when there it is not a visible
                             ;; assets we make an initialization round
                             [::tokens-found balances]
                             [::update-tokens-balances-success balances]))))
    :on-error
    #(re-frame/dispatch [::update-token-balance-fail %])}))

(re-frame/reg-fx
 :wallet/get-tokens-balances
 get-token-balances)

(defn rpc->token [tokens]
  (reduce (fn [acc {:keys [address] :as token}]
            (assoc acc
                   address
                   (assoc token :custom? true)))
          {}
          tokens))

(fx/defn initialize-tokens
  [{:keys [db]} custom-tokens]
  (let [all-default-tokens (get tokens/all-default-tokens
                                (ethereum/chain-keyword db))
        default-tokens (utils.core/index-by :address all-default-tokens)
        ;;we want to override custom-tokens by default
        all-tokens     (merge (rpc->token custom-tokens) default-tokens)]
    (merge
     {:db (assoc db :wallet/all-tokens all-tokens)}
     (when config/erc20-contract-warnings-enabled?
       {:wallet/validate-tokens [default-tokens all-default-tokens]}))))

(fx/defn initialize-favourites
  [{:keys [db]} favourites]
  {:db (assoc db :wallet/favourites (reduce (fn [acc {:keys [address] :as favourit}]
                                              (assoc acc address favourit))
                                            {}
                                            favourites))})

(fx/defn update-balances
  {:events [:wallet/update-balances]}
  [{{:keys [network-status :wallet/all-tokens
            multiaccount :multiaccount/accounts] :as db} :db
    :as cofx} addresses init?]
  (let [addresses (or addresses (map (comp string/lower-case :address) accounts))
        {:keys [:wallet/visible-tokens]} multiaccount
        chain     (ethereum/chain-keyword db)
        assets    (get visible-tokens chain)
        tokens    (->> (vals all-tokens)
                       (remove #(or (:hidden? %)
                                    ;;if not init remove not visible tokens
                                    (and (not init?)
                                         (not (get assets (:symbol %))))))
                       (reduce (fn [acc {:keys [address symbol]}]
                                 (assoc acc address symbol))
                               {}))]
    (when (not= network-status :offline)
      (fx/merge
       cofx
       {:wallet/get-balances        addresses
        :wallet/get-tokens-balances {:addresses addresses
                                     :tokens    tokens
                                     :assets    assets
                                     :init?     init?}
        :db                         (prices/clear-error-message db :balance-update)}
       (when-not assets
         (multiaccounts.update/multiaccount-update
          :wallet/visible-tokens (assoc visible-tokens chain (or (config/default-visible-tokens chain)
                                                                 #{}))
          {}))))))

(defn- set-checked [tokens-id token-id checked?]
  (let [tokens-id (or tokens-id #{})]
    (if checked?
      (conj tokens-id token-id)
      (disj tokens-id token-id))))

(fx/defn update-balance
  {:events [::update-balance-success]}
  [{:keys [db]} address balance]
  {:db (assoc-in db
                 [:wallet :accounts (eip55/address->checksum address) :balance :ETH]
                 (money/bignumber balance))})

(fx/defn update-toggle-in-settings
  [{{:keys [multiaccount] :as db} :db :as cofx} symbol checked?]
  (let [chain          (ethereum/chain-keyword db)
        visible-tokens (get multiaccount :wallet/visible-tokens)]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :wallet/visible-tokens (update visible-tokens
                                              chain
                                              #(set-checked % symbol checked?))
               {})
              #(when checked?
                 (update-balances % nil nil)))))

(fx/defn toggle-visible-token
  [cofx symbol checked?]
  (update-toggle-in-settings cofx symbol checked?))

(fx/defn update-tokens-balances
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

(fx/defn configure-token-balance-and-visibility
  {:events [::tokens-found]}
  [{:keys [db] :as cofx} balances]
  (let [chain (ethereum/chain-keyword db)
        visible-tokens (get-in db [:multiaccount :wallet/visible-tokens])
        chain-visible-tokens (into (or (config/default-visible-tokens chain)
                                       #{})
                                   (flatten (map keys (vals balances))))]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :wallet/visible-tokens (assoc visible-tokens
                                             chain
                                             chain-visible-tokens)
               {})
              (update-tokens-balances balances)
              (prices/update-prices))))

(fx/defn add-custom-token
  [cofx {:keys [symbol]}]
  (update-toggle-in-settings cofx symbol true))

(fx/defn remove-custom-token
  [cofx {:keys [symbol]}]
  (update-toggle-in-settings cofx symbol false))

(fx/defn set-and-validate-amount
  {:events [:wallet.send/set-amount-text]}
  [{:keys [db]} amount]
  {:db (assoc-in db [:wallet/prepare-transaction :amount-text] amount)})

(fx/defn wallet-send-gas-price-success
  {:events [:wallet.send/update-gas-price-success]}
  [{db :db} price]
  {:db (assoc-in db [:wallet/prepare-transaction :gasPrice] price)})

(fx/defn set-max-amount
  {:events [:wallet.send/set-max-amount]}
  [{:keys [db]} {:keys [amount decimals symbol]}]
  (let [^js gas (money/bignumber 21000)
        ^js gasPrice (get-in db [:wallet/prepare-transaction :gasPrice])
        ^js fee (when gasPrice (.times gas gasPrice))
        amount-text (if (= :ETH symbol)
                      (when (and fee (money/sufficient-funds? fee amount))
                        (str (wallet.utils/format-amount (.minus amount fee) decimals)))
                      (str (wallet.utils/format-amount amount decimals)))]
    (when amount-text
      {:db (assoc-in db [:wallet/prepare-transaction :amount-text] amount-text)})))

(fx/defn set-and-validate-request-amount
  {:events [:wallet.request/set-amount-text]}
  [{:keys [db]} amount]
  {:db (assoc-in db [:wallet/prepare-transaction :amount-text] amount)})

(fx/defn request-transaction-button-clicked-from-chat
  {:events  [:wallet.ui/request-transaction-button-clicked]}
  [{:keys [db] :as cofx} {:keys [to amount from token]}]
  (let [{:keys [symbol address]} token
        from-address (:address from)
        identity (:current-chat-id db)]
    (fx/merge cofx
              {:db (dissoc db :wallet/prepare-transaction)
               ::json-rpc/call [{:method (json-rpc/call-ext-method "requestTransaction")
                                 :params [(:public-key to)
                                          amount
                                          (when-not (= symbol :ETH)
                                            address)
                                          from-address]
                                 :on-success #(re-frame/dispatch [:transport/message-sent % 1])}]})))

(fx/defn accept-request-transaction-button-clicked-from-command
  {:events  [:wallet.ui/accept-request-transaction-button-clicked-from-command]}
  [{:keys [db]} chat-id {:keys [value contract] :as request-parameters}]
  (let [identity (:current-chat-id db)
        all-tokens (:wallet/all-tokens db)
        current-network-string  (:networks/current-network db)
        all-networks (:networks/networks db)
        current-network (get all-networks current-network-string)
        chain (ethereum/network->chain-keyword current-network)
        {:keys [symbol decimals]}
        (if (seq contract)
          (get all-tokens contract)
          (tokens/native-currency chain))
        amount-text (str (money/internal->formatted value symbol decimals))]
    {:db (assoc db :wallet/prepare-transaction
                {:from (ethereum/get-default-account (:multiaccount/accounts db))
                 :to   (or (get-in db [:contacts/contacts identity])
                           (-> identity
                               contact.db/public-key->new-contact
                               contact.db/enrich-contact))
                 :request-parameters request-parameters
                 :chat-id chat-id
                 :symbol symbol
                 :amount-text amount-text
                 :request? true
                 :from-chat? true})
     :dispatch [:navigate-to :prepare-send-transaction]}))

(fx/defn set-and-validate-amount-request
  {:events [:wallet.request/set-and-validate-amount]}
  [{:keys [db]} amount symbol decimals]
  (let [{:keys [value error]} (wallet.db/parse-amount amount decimals)]
    {:db (-> db
             (assoc-in [:wallet :request-transaction :amount] (money/formatted->internal value symbol decimals))
             (assoc-in [:wallet :request-transaction :amount-text] amount)
             (assoc-in [:wallet :request-transaction :amount-error] error))}))

(fx/defn set-symbol-request
  {:events [:wallet.request/set-symbol]}
  [{:keys [db]} symbol]
  {:db (assoc-in db [:wallet :request-transaction :symbol] symbol)})

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(fx/defn on-recipient-address-resolved
  {:events [::recipient-address-resolved]}
  [{:keys [db]} address]
  {:db (assoc-in db [:wallet/prepare-transaction :to :address] address)
   :signing/update-gas-price {:success-event :wallet.send/update-gas-price-success}})

(fx/defn prepare-transaction-from-chat
  {:events [:wallet/prepare-transaction-from-chat]}
  [{:keys [db]}]
  (let [chain (ethereum/chain-keyword db)
        identity (:current-chat-id db)
        {:keys [ens-verified name] :as contact}
        (or (get-in db [:contacts/contacts identity])
            (-> identity
                contact.db/public-key->new-contact
                contact.db/enrich-contact))]
    (cond-> {:db (assoc db
                        :wallet/prepare-transaction
                        {:from (ethereum/get-default-account
                                (:multiaccount/accounts db))
                         :to contact
                         :symbol :ETH
                         :from-chat? true})
             :dispatch [:navigate-to :prepare-send-transaction]}
      ens-verified
      (assoc ::resolve-address
             {:registry (get ens/ens-registries chain)
              :ens-name (if (= (.indexOf ^js name ".") -1)
                          (stateofus/subdomain name)
                          name)
              ;;TODO handle errors and timeout for ens name resolution
              :cb #(re-frame/dispatch [::recipient-address-resolved %])}))))

(fx/defn prepare-request-transaction-from-chat
  {:events [:wallet/prepare-request-transaction-from-chat]}
  [{:keys [db]}]
  (let [identity (:current-chat-id db)]
    {:db (assoc db :wallet/prepare-transaction
                {:from (ethereum/get-default-account (:multiaccount/accounts db))
                 :to   (or (get-in db [:contacts/contacts identity])
                           (-> identity
                               contact.db/public-key->new-contact
                               contact.db/enrich-contact))
                 :symbol :ETH
                 :from-chat? true
                 :request-command? true})
     :dispatch [:navigate-to :request-transaction]}))

(fx/defn prepare-transaction-from-wallet
  {:events [:wallet/prepare-transaction-from-wallet]}
  [{:keys [db]} account]
  {:db (assoc db :wallet/prepare-transaction
              {:from       account
               :to         nil
               :symbol     :ETH
               :from-chat? false})
   :dispatch [:navigate-to :prepare-send-transaction]
   :signing/update-gas-price {:success-event :wallet.send/update-gas-price-success}})

(fx/defn cancel-transaction-command
  {:events [:wallet/cancel-transaction-command]}
  [{:keys [db]}]
  (let [identity (:current-chat-id db)]
    {:db (dissoc db :wallet/prepare-transaction)}))

(fx/defn finalize-transaction-from-command
  {:events [:wallet/finalize-transaction-from-command]}
  [{:keys [db]} account to symbol amount]
  {:db (assoc db :wallet/prepare-transaction
              {:from       account
               :to         to
               :symbol     symbol
               :amount     amount
               :from-command? true})})

(fx/defn view-only-qr-scanner-allowed
  {:events [:wallet.add-new/qr-scanner]}
  [{:keys [db] :as cofx} options]
  (fx/merge cofx
            {:db (update-in db [:add-account] dissoc :address)}
            (qr-scaner/scan-qr-code options)))

(fx/defn wallet-send-set-symbol
  {:events [:wallet.send/set-symbol]}
  [{:keys [db] :as cofx} symbol]
  (fx/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction :symbol] symbol)}
            (bottom-sheet/hide-bottom-sheet)))

(fx/defn wallet-send-set-field
  {:events [:wallet.send/set-field]}
  [{:keys [db] :as cofx} field value]
  (fx/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction field] value)}
            (bottom-sheet/hide-bottom-sheet)))

(fx/defn wallet-request-set-field
  {:events [:wallet.request/set-field]}
  [{:keys [db] :as cofx} field value]
  (fx/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction field] value)}
            (bottom-sheet/hide-bottom-sheet)))

(fx/defn navigate-to-recipient-code
  {:events [:wallet.send/navigate-to-recipient-code]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc :wallet/recipient {}))}
            (bottom-sheet/hide-bottom-sheet)
            (navigation/navigate-to-cofx :recipient nil)))

(fx/defn show-delete-account-confirmation
  {:events [:wallet.settings/show-delete-account-confirmation]}
  [_ account]
  {:ui/show-confirmation {:title               (i18n/label :t/are-you-sure?)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [:wallet.accounts/delete-account account])
                          :on-cancel           #()}})

(re-frame/reg-fx
 ::stop-wallet
 (fn []
   (log/info "stop-wallet fx")
   (status/stop-wallet)))

(re-frame/reg-fx
 ::start-wallet
 (fn [watch-new-blocks?]
   (log/info "start-wallet fx" watch-new-blocks?)
   (status/start-wallet watch-new-blocks?)))

(def ms-10-min (datetime/minutes 10))
(def ms-20-min (datetime/minutes 20))

(def ms-2-min (datetime/minutes 2))
(def ms-4-min (datetime/minutes 4))

(defn get-restart-interval [db]
  (if (ethereum/custom-rpc-node?
       (ethereum/current-network db))
    ms-4-min
    ms-20-min))

(defn get-watching-interval [db]
  (if (ethereum/custom-rpc-node?
       (ethereum/current-network db))
    ms-2-min
    ms-10-min))

(fx/defn stop-wallet
  [{:keys [db] :as cofx}]
  (let [state (get db :wallet-service/state)
        old-timeout (get db :wallet-service/restart-timeout)
        timeout (or
                 old-timeout
                 (utils.utils/set-timeout
                  #(re-frame.core/dispatch [::restart])
                  (get-restart-interval db)))]
    {:db           (-> db
                       (update :wallet dissoc :fetching)
                       (assoc :wallet-service/state :stopped)
                       (assoc :wallet-service/restart-timeout timeout)
                       (dissoc :wallet/recent-history-fetching-started?
                               :wallet/waiting-for-recent-history?
                               :wallet/refreshing-history?))
     ::stop-wallet nil}))

(fx/defn start-wallet
  [{:keys [db] :as cofx} watch-new-blocks?]
  (let [old-timeout (get db :wallet-service/restart-timeout)
        state       (get db :wallet-service/state)
        timeout     (utils.utils/set-timeout
                     #(re-frame.core/dispatch [::restart])
                     (get-restart-interval db))]
    {:db            (-> db
                        (assoc :wallet-service/state :started)
                        (assoc :wallet-service/restart-timeout timeout))
     ::start-wallet watch-new-blocks?
     ::utils.utils/clear-timeouts
     [old-timeout]}))

(fx/defn restart-wallet-service
  [{:keys [db] :as cofx}
   {:keys [force-start? watch-new-blocks? ignore-syncing-settings?]}]
  (when (or force-start? (:multiaccount db))
    (let [watching-txs? (get db :wallet/watch-txs)
          waiting? (get db :wallet/waiting-for-recent-history?)
          syncing-allowed? (mobile-network-utils/syncing-allowed? cofx)]
      (log/info "restart-wallet-service"
                "force-start?" force-start?
                "watching-txs?" watching-txs?
                "syncing-allowed?" syncing-allowed?
                "watch-new-blocks?" watch-new-blocks?)
      (if (and (or syncing-allowed?
                   ignore-syncing-settings?)
               (or
                waiting?
                force-start?
                watching-txs?))
        (start-wallet cofx (boolean (or watch-new-blocks? watching-txs?)))
        (stop-wallet cofx)))))

(def background-cooldown-time (datetime/minutes 3))

(fx/defn restart-wallet-service-after-background
  [{:keys [now] :as cofx} background-time]
  (when (> (- now background-time)
           background-cooldown-time)
    (restart-wallet-service
     cofx
     {:force-start? true})))

(fx/defn restart-wallet-service-default
  [cofx]
  (restart-wallet-service cofx nil))

(fx/defn restart
  {:events [::restart]}
  [{:keys [db] :as cofx}]
  (restart-wallet-service cofx {:force-start? true}))

(def pull-to-refresh-cooldown-period (* 1 60 1000))

(fx/defn restart-on-pull
  {:events [:wallet.ui/pull-to-refresh-history]}
  [{:keys [db now] :as cofx}]
  (let [last-pull         (get db :wallet/last-pull-time)
        watching?         (get db :wallet/watch-txs)
        fetching-history? (get db :wallet/recent-history-fetching-started?)]
    (when (and (not watching?)
               (not fetching-history?)
               (or (not last-pull)
                   (> (- now last-pull) pull-to-refresh-cooldown-period)))
      (fx/merge
       {:db (assoc db
                   :wallet/last-pull-time now
                   :wallet/refreshing-history? true)}
       (restart-wallet-service
        {:force-start?             true
         :ignore-syncing-settings? true})))))

(fx/defn watch-tx
  {:events [:watch-tx]}
  [{:keys [db] :as cofx} tx-id]
  (let [txs         (get db :wallet/watch-txs)
        old-timeout (get db :wallet-service/restart-timeout)
        timeout     (utils.utils/set-timeout
                     (fn []
                       (re-frame.core/dispatch [::stop-watching-txs]))
                     (get-watching-interval db))]
    (fx/merge
     {:db (-> db
              (update :wallet/watch-txs (fnil conj #{}) tx-id)
              (assoc :wallet/watch-txs-timeout timeout))
      ::utils.utils/clear-timeouts
      [old-timeout]}
     (restart-wallet-service {:force-start?      true
                              :watch-new-blocks? true}))))

(fx/defn stop-watching-txs
  {:events [::stop-watching-txs]}
  [{:keys [db] :as cofx}]
  (fx/merge
   {:db (dissoc db
                :wallet/watch-txs
                :wallet/watch-txs-timeout)}
   (restart-wallet-service-default)))

(fx/defn stop-watching-tx
  [{:keys [db] :as cofx} tx]
  (let [txs (get db :wallet/watch-txs)
        new-txs ((fnil disj #{}) txs tx)]
    (when (get txs tx)
      (if (empty? new-txs)
        (stop-watching-txs cofx)
        {:db (assoc db :wallet/watch-txs new-txs)}))))

(fx/defn clear-timeouts
  [{:keys [db]}]
  (let [watch-timeout-id (get db :wallet/watch-txs-timeout)
        restart-timeout-id (get db :wallet-service/restart-timeout)]
    {:db                          (dissoc db :wallet/watch-txs-timeout
                                          :wallet-service/restart-timeout)
     ::utils.utils/clear-timeouts [watch-timeout-id restart-timeout-id]}))

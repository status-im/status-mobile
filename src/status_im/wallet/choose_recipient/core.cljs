(ns status-im.wallet.choose-recipient.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.i18n :as i18n]
            [status-im.utils.money :as money]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]))

(fx/defn toggle-flashlight
  {:events [:wallet/toggle-flashlight]}
  [{:keys [db]}]
  (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
        toggled-state (if (= :on flashlight-state) :off :on)]
    {:db (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state)}))

(defn- find-address-name [db address]
  (:name (contact.db/find-contact-by-address (:contacts/contacts db) address)))

(defn- fill-request-details [db {:keys [address name value symbol gas gasPrice public-key from-chat?]} request?]
  {:pre [(not (nil? address))]}
  (let [name (or name (find-address-name db address))
        ;;TODO request isn't implemented
        data-path (if request?
                    :wallet/prepare-transaction
                    :wallet/prepare-transaction)]
    (update db data-path
            (fn [{old-symbol :symbol :as old-transaction}]
              (let [symbol-changed? (not= old-symbol symbol)]
                (cond-> (assoc old-transaction
                               :to address :to-name name :public-key public-key
                               :gas (money/bignumber gas)
                               :gas-price (money/bignumber gasPrice))
                  value (assoc :amount value)
                  symbol (assoc :symbol symbol)
                  from-chat? (assoc :from-chat? from-chat?)))))))

;;TODO request isn't implemented
(fx/defn fill-request-from-contact
  {:events [:wallet/fill-request-from-contact]}
  [{db :db} {:keys [address name public-key]} request?]
  {:db       (fill-request-details db {:address address :name name :public-key public-key} request?)
   :dispatch [:navigate-back]})

(defn- extract-details
  "First try to parse as EIP681 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` and `chain-id` keys"
  [m chain-id all-tokens]
  (or (merge m (eip681/extract-request-details m all-tokens))
      (when (ethereum/address? m)
        {:address m :chain-id chain-id})))

;; NOTE(janherich) - whenever changing assets, we want to clear the previusly set amount/amount-text
(defn changed-asset [{:keys [db] :as fx} old-symbol new-symbol]
  (-> fx
      (assoc-in [:db :wallet/prepare-transaction :amount] nil)
      (assoc-in [:db :wallet/prepare-transaction :amount-text] nil)
      (assoc-in [:db :wallet/prepare-transaction :asset-error]
                (i18n/label :t/changed-asset-warning {:old old-symbol :new new-symbol}))))

(defn changed-amount-warning [fx old-amount new-amount]
  (assoc-in fx [:db :wallet/prepare-transaction :amount-error]
            (i18n/label :t/changed-amount-warning {:old old-amount :new new-amount})))

(defn use-default-eth-gas [fx]
  (assoc-in fx [:db :wallet :send-transaction :gas]
            ethereum/default-transaction-gas))

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(re-frame/reg-fx
 ::resolve-addresses
 (fn [{:keys [registry ens-names callback]}]
   ;; resolve all addresses then call the callback function with the array of 
   ;;addresses as parameter
   (-> (js/Promise.all
        (clj->js (mapv (fn [ens-name]
                         (js/Promise.
                          (fn [resolve reject]
                            (ens/get-addr registry ens-name resolve))))
                       ens-names)))
       (.then callback)
       (.catch (fn [error]
                 (js/console.log error))))))

(fx/defn set-recipient
  {:events [:wallet.send/set-recipient ::recipient-address-resolved]}
  [{:keys [db]} recipient]
  (let [chain (ethereum/chain-keyword db)]
    (if (ens/is-valid-eth-name? recipient)
      {::resolve-address {:registry (get ens/ens-registries chain)
                          :ens-name recipient
                          :cb       #(re-frame/dispatch [::recipient-address-resolved %])}}
      (if (ethereum/address? recipient)
        (let [checksum (eip55/address->checksum recipient)]
          (if (eip55/valid-address-checksum? checksum)
            {:db       (-> db
                           (assoc-in [:wallet/prepare-transaction :to] checksum)
                           (assoc-in [:wallet/prepare-transaction :modal-opened?] false))
             :dispatch [:navigate-back]}
            {:ui/show-error (i18n/label :t/wallet-invalid-address-checksum {:data recipient})}))
        {:ui/show-error (i18n/label :t/wallet-invalid-address {:data recipient})}))))

(fx/defn request-uri-parsed
  {:events [:wallet/request-uri-parsed]}
  [{{:networks/keys [current-network] :wallet/keys [all-tokens] :as db} :db} data origin]
  (let [current-chain-id                       (get-in constants/default-networks [current-network :config :NetworkId])
        {:keys [address chain-id] :as details} (extract-details data current-chain-id all-tokens)
        valid-network?   (boolean (= current-chain-id chain-id))
        previous-state   (get db :wallet/prepare-transaction)
        old-symbol       (:symbol previous-state)
        new-symbol       (:symbol details)
        old-amount       (:amount previous-state)
        new-amount       (:value details)
        symbol-changed?  (and old-symbol new-symbol (not= old-symbol new-symbol))
        amount-changed?  (and old-amount new-amount (not= old-amount new-amount))]
    (cond-> {:db db}
      (not= :deep-link origin) (assoc :dispatch [:navigate-back]) ;; Only navigate-back when called from within wallet
      symbol-changed? (changed-asset old-symbol new-symbol)
      (and address valid-network?) (update :db #(fill-request-details % details false))
      (and old-amount new-amount (not= old-amount new-amount)) (changed-amount-warning old-amount new-amount)
       ;; NOTE(goranjovic) - the next line is there is because QR code scanning switches the amount to ETH
       ;; automatically, so we need to update the gas limit accordingly. The check for origin screen is there
       ;; so that we wouldn't also switch gas limit to ETH specific if the user pastes address as text.
       ;; We need to check if address is defined so that we wouldn't trigger this behavior when invalid QR is scanned
       ;; (e.g. public-key)
      (and address (= origin :qr) (not new-gas) symbol-changed?) (use-default-eth-gas)
      (not address) (assoc :ui/show-error (i18n/label :t/wallet-invalid-address {:data data}))
      (and address (not valid-network?))
      (assoc :ui/show-error (i18n/label :t/wallet-invalid-chain-id
                                        {:data data :chain current-chain-id})))))

(fx/defn qr-scanner-result
  {:events [:wallet.send/qr-scanner-result]}
  [{db :db :as cofx} data opts]
  (fx/merge cofx
            {:db (assoc-in db [:wallet/prepare-transaction :modal-opened?] false)}
            (navigation/navigate-back)
            (fill-request-from-url data)))

(fx/defn qr-scanner-cancel
  {:events [:wallet.send/qr-scanner-cancel]}
  [{db :db} _]
  {:db (assoc-in db [:wallet/prepare-transaction :modal-opened?] false)})
(fx/defn fill-request-from-contact
  {:events [:wallet/fill-request-from-contact]}
  [{db :db} {:keys [address name public-key]} request?]
  {:db         (fill-request-details db {:address address :name name :public-key public-key} request?)
   :dispatch   [:navigate-back]})

(fx/defn resolve-ens-addresses
  {:events [:wallet.send/resolve-ens-addresses :wallet.send/qr-code-request-scanned]}
  [{{:networks/keys [current-network] :wallet/keys [all-tokens] :as db} :db}
   uri origin]
  (when-let [message (eip681/parse-uri uri)]
  ;; first we get a vector of ens-names to resolve and a vector of paths of
  ;; these names
    (let [{:keys [paths ens-names]}
          (reduce (fn [acc path]
                    (let [address (get-in message path)]
                      (if (ens/is-valid-eth-name? address)
                        (-> acc
                            (update :paths conj path)
                            (update :ens-names conj address))
                        acc)))
                  {:paths [] :ens-names []}
                  [[:address] [:function-arguments :address]])]
      (if (empty? ens-names)
      ;; if there is no ens-names, we dispatch request-uri-parsed immediately
        (request-uri-parsed message origin)
        {::resolve-addresses
         {:registry (get ens/ens-registries (ethereum/chain-keyword db))
          :ens-names ens-names
          :callback
          (fn [addresses]
            (re-frame/dispatch
             [:wallet/request-uri-parsed
            ;; we replace the ens-names at their path in the message by their
            ;; actual address
              (reduce (fn [message [path address]]
                        (assoc-in message path address))
                      message
                      (map vector paths addresses))
              origin]))}}))))

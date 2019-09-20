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
            [status-im.utils.fx :as fx]))

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
        data-path (if request?
                    [:wallet :request-transaction]
                    [:wallet :send-transaction])]
    (update-in db data-path
               (fn [{old-symbol :symbol :as old-transaction}]
                 (let [symbol-changed? (not= old-symbol symbol)]
                   (cond-> (assoc old-transaction :to address :to-name name :public-key public-key)
                     value (assoc :amount value)
                     symbol (assoc :symbol symbol)
                     (and gas symbol-changed?) (assoc :gas (money/bignumber gas))
                     from-chat? (assoc :from-chat? from-chat?)
                     (and gasPrice symbol-changed?)
                     (assoc :gas-price (money/bignumber gasPrice))
                     (and symbol (not gasPrice) symbol-changed?)
                     (assoc :gas-price (ethereum/estimate-gas symbol))))))))

(defn- extract-details
  "First try to parse as EIP681 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` and `chain-id` keys"
  [s chain-id all-tokens]
  (or (let [m (eip681/parse-uri s)]
        (merge m (eip681/extract-request-details m all-tokens)))
      (when (ethereum/address? s)
        {:address s :chain-id chain-id})))

;; NOTE(janherich) - whenever changing assets, we want to clear the previusly set amount/amount-text
(defn changed-asset [{:keys [db] :as fx} old-symbol new-symbol]
  (-> fx
      (merge {:wallet/update-gas-price
              {:success-event :wallet/update-gas-price-success
               :edit?         false}})
      (assoc-in [:db :wallet :send-transaction :amount] nil)
      (assoc-in [:db :wallet :send-transaction :amount-text] nil)
      (assoc-in [:db :wallet :send-transaction :asset-error]
                (i18n/label :t/changed-asset-warning {:old old-symbol :new new-symbol}))))

(defn changed-amount-warning [fx old-amount new-amount]
  (assoc-in fx [:db :wallet :send-transaction :amount-error]
            (i18n/label :t/changed-amount-warning {:old old-amount :new new-amount})))

(defn use-default-eth-gas [fx]
  (assoc-in fx [:db :wallet :send-transaction :gas]
            ethereum/default-transaction-gas))

(re-frame/reg-fx
 :resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(fx/defn set-recipient
  {:events [:wallet.send/set-recipient]}
  [{:keys [db]} recipient]
  (let [chain (ethereum/chain-keyword db)]
    (if (ens/is-valid-eth-name? recipient)
      {:resolve-address {:registry (get ens/ens-registries chain)
                         :ens-name recipient
                         :cb       #(re-frame/dispatch [:wallet.send/set-recipient %])}}
      (if (ethereum/address? recipient)
        (let [checksum (eip55/address->checksum recipient)]
          (if (eip55/valid-address-checksum? checksum)
            {:db       (assoc-in db [:wallet :send-transaction :to] checksum)
             :dispatch [:navigate-back]}
            {:ui/show-error (i18n/label :t/wallet-invalid-address-checksum {:data recipient})}))
        {:ui/show-error (i18n/label :t/wallet-invalid-address {:data recipient})}))))

(fx/defn fill-request-from-url
  {:events [:wallet/fill-request-from-url]}
  [{{:keys [network] :wallet/keys [all-tokens] :as db} :db} data origin]
  (let [current-chain-id                       (get-in constants/default-networks [network :config :NetworkId])
        {:keys [address chain-id] :as details} (extract-details data current-chain-id all-tokens)
        valid-network?                         (boolean (= current-chain-id chain-id))
        previous-state                         (get-in db [:wallet :send-transaction])
        old-symbol                             (:symbol previous-state)
        new-symbol                             (:symbol details)
        old-amount                             (:amount previous-state)
        new-amount                             (:value details)
        new-gas                                (:gas details)
        symbol-changed?                        (and old-symbol new-symbol (not= old-symbol new-symbol))]
    (cond-> {:db db}
      (not= :deep-link origin) (assoc :dispatch [:navigate-back]) ;; Only navigate-back when called from within wallet
      (and address valid-network?) (update :db #(fill-request-details % details false))
      symbol-changed? (changed-asset old-symbol new-symbol)
      (and old-amount new-amount (not= old-amount new-amount)) (changed-amount-warning old-amount new-amount)
       ;; NOTE(goranjovic) - the next line is there is because QR code scanning switches the amount to ETH
       ;; automatically, so we need to update the gas limit accordingly. The check for origin screen is there
       ;; so that we wouldn't also switch gas limit to ETH specific if the user pastes address as text.
       ;; We need to check if address is defined so that we wouldn't trigger this behavior when invalid QR is scanned
       ;; (e.g. public-key)
      (and address (= origin :qr) (not new-gas) symbol-changed?) (use-default-eth-gas)
      (not address) (assoc :ui/show-error (i18n/label :t/wallet-invalid-address {:data data}))
      (and address (not valid-network?)) (assoc :ui/show-error (i18n/label :t/wallet-invalid-chain-id {:data data :chain current-chain-id})))))

(fx/defn fill-request-from-contact
  {:events [:wallet/fill-request-from-contact]}
  [{db :db} {:keys [address name public-key]} request?]
  {:db         (fill-request-details db {:address address :name name :public-key public-key} request?)
   :dispatch   [:navigate-back]})

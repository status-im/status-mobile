(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.wallet.choose-recipient.request-details
             :as request-details]))

(handlers/register-handler-fx
 :wallet/toggle-flashlight
 (fn [{:keys [db]}]
   (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
         toggled-state (if (= :on flashlight-state) :off :on)]
     {:db (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state)})))

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
            (ethereum/default-transaction-gas)))

(re-frame/reg-fx
 :resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(handlers/register-handler-fx
 :wallet.send/set-recipient
 (fn [{:keys [db]} [_ recipient]]
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
         {:ui/show-error (i18n/label :t/wallet-invalid-address {:data recipient})})))))

(handlers/register-handler-fx
 :wallet/fill-request-from-url
 (fn [{{:keys [network] :wallet/keys [all-tokens] :as db} :db} [_ data origin]]
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
       (and address valid-network?) (update :db #(request-details/fill-request-details % details false))
       symbol-changed? (changed-asset old-symbol new-symbol)
       (and old-amount new-amount (not= old-amount new-amount)) (changed-amount-warning old-amount new-amount)
        ;; NOTE(goranjovic) - the next line is there is because QR code scanning switches the amount to ETH
        ;; automatically, so we need to update the gas limit accordingly. The check for origin screen is there
        ;; so that we wouldn't also switch gas limit to ETH specific if the user pastes address as text.
        ;; We need to check if address is defined so that we wouldn't trigger this behavior when invalid QR is scanned
        ;; (e.g. public-key)
       (and address (= origin :qr) (not new-gas) symbol-changed?) (use-default-eth-gas)
       (not address) (assoc :ui/show-error (i18n/label :t/wallet-invalid-address {:data data}))
       (and address (not valid-network?)) (assoc :ui/show-error (i18n/label :t/wallet-invalid-chain-id {:data data :chain current-chain-id}))))))

(handlers/register-handler-fx
 :wallet/fill-request-from-contact
 (fn [{db :db} [_ {:keys [address name public-key]} request?]]
   {:db         (request-details/fill-request-details db {:address address :name name :public-key public-key} request?)
    :dispatch   [:navigate-back]}))

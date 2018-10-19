(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.utils.ethereum.ens :as ens]
            [re-frame.core :as re-frame]))

(handlers/register-handler-fx
 :wallet/toggle-flashlight
 (fn [{:keys [db]}]
   (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
         toggled-state (if (= :on flashlight-state) :off :on)]
     {:db (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state)})))

(defn- fill-request-details [db {:keys [address name value symbol gas gasPrice whisper-identity from-chat?]}]
  {:pre [(not (nil? address))]}
  (update-in
   db [:wallet :send-transaction]
   (fn [{old-symbol :symbol :as old-transaction}]
     (let [symbol-changed? (not= old-symbol symbol)]
       (cond-> (assoc old-transaction :to address :to-name name :whisper-identity whisper-identity)
         value (assoc :amount value)
         symbol (assoc :symbol symbol)
         (and gas symbol-changed?) (assoc :gas (money/bignumber gas))
         from-chat? (assoc :from-chat? from-chat?)
         (and gasPrice symbol-changed?)
         (assoc :gas-price (money/bignumber gasPrice))
         (and symbol (not gasPrice) symbol-changed?)
         (assoc :gas-price (ethereum/estimate-gas symbol)))))))

(defn- extract-details
  "First try to parse as EIP681 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` and `chain-id` keys"
  [s chain-id]
  (or (let [m (eip681/parse-uri s)]
        (merge m (eip681/extract-request-details m)))
      (when (ethereum/address? s)
        {:address s :chain-id chain-id})))

;; NOTE(janherich) - whenever changing assets, we want to clear the previusly set amount/amount-text
(defn changed-asset [{:keys [db] :as fx} old-symbol new-symbol]
  (-> fx
      (merge {:update-gas-price {:web3          (:web3 db)
                                 :success-event :wallet/update-gas-price-success
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
 (fn [{:keys [web3 registry ens-name cb]}]
   (ens/get-addr web3 registry ens-name cb)))

(handlers/register-handler-fx
 :wallet.send/set-recipient
 (fn [{:keys [db]} [_ recipient]]
   (let [{:keys [web3 network]} db
         network-info (get-in db [:account/account :networks network])
         chain (ethereum/network->chain-keyword network-info)]
     (if (ens/is-valid-eth-name? recipient)
       {:resolve-address {:web3     web3
                          :registry (get ens/ens-registries chain)
                          :ens-name recipient
                          :cb       #(re-frame/dispatch [:wallet.send/set-recipient %])}}
       (if (ethereum/address? recipient)
         {:db       (assoc-in db [:wallet :send-transaction :to] recipient)
          :dispatch [:navigate-back]}
         {:ui/show-error (i18n/label :t/wallet-invalid-address {:data recipient})})))))

(handlers/register-handler-fx
 :wallet/fill-request-from-url
 (fn [{{:keys [network] :as db} :db} [_ data origin]]
   (let [current-chain-id                       (get-in constants/default-networks [network :config :NetworkId])
         {:keys [address chain-id] :as details} (extract-details data current-chain-id)
         valid-network?                         (boolean (= current-chain-id chain-id))
         previous-state                         (get-in db [:wallet :send-transaction])
         old-symbol                             (:symbol previous-state)
         new-symbol                             (:symbol details)
         old-amount                             (:amount previous-state)
         new-amount                             (:value details)
         new-gas                                (:gas details)
         symbol-changed?                        (and old-symbol new-symbol (not= old-symbol new-symbol))]
     (cond-> {:db         db
              :dispatch   [:navigate-back]}
       (and address valid-network?) (update :db #(fill-request-details % details))
       symbol-changed? (changed-asset old-symbol new-symbol)
       (and old-amount new-amount (not= old-amount new-amount)) (changed-amount-warning old-amount new-amount)
       ;; NOTE(goranjovic) - the next line is there is because QR code scanning switches the amount to ETH
       ;; automatically, so we need to update the gas limit accordingly. The check for origin screen is there
       ;; so that we wouldn't also switch gas limit to ETH specific if the user pastes address as text.
       ;; We need to check if address is defined so that we wouldn't trigger this behavior when invalid QR is scanned
       ;; (e.g. whisper-id)
       (and address (= origin :qr) (not new-gas) symbol-changed?) (use-default-eth-gas)
       (not address) (assoc :ui/show-error (i18n/label :t/wallet-invalid-address {:data data}))
       (and address (not valid-network?)) (assoc :ui/show-error (i18n/label :t/wallet-invalid-chain-id {:data data :chain current-chain-id}))))))

(handlers/register-handler-fx
 :wallet/fill-request-from-contact
 (fn [{db :db} [_ {:keys [address name whisper-identity]}]]
   {:db         (fill-request-details db {:address address :name name :whisper-identity whisper-identity})
    :dispatch   [:navigate-back]}))

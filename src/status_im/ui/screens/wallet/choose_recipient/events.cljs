(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]))

(handlers/register-handler-db
 :wallet/toggle-flashlight
 (fn [db]
   (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
         toggled-state (if (= :on flashlight-state) :off :on)]
     (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state))))

(defn- fill-request-details [db {:keys [address name value symbol gas gasPrice whisper-identity from-chat?]}]
  {:pre [(not (nil? address))]}
  (update-in
   db [:wallet :send-transaction]
   #(cond-> (assoc % :to address :to-name name :whisper-identity whisper-identity)
      value       (assoc :amount value)
      symbol      (assoc :symbol symbol)
      gas         (assoc :gas (money/bignumber gas))
      gasPrice    (assoc :gas-price (money/bignumber gasPrice))
      from-chat?  (assoc :from-chat? from-chat?)
      (and symbol (not gasPrice))
      (assoc :gas-price (ethereum/estimate-gas symbol)))))

(defn- extract-details
  "First try to parse as EIP681 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` and `chain-id` keys"
  [s chain-id]
  (or (let [m (eip681/parse-uri s)]
        (merge m (eip681/extract-request-details m)))
      (when (ethereum/address? s)
        {:address s :chain-id chain-id})))

(defn changed-asset-warning [cofx old-symbol new-symbol]
  (assoc-in cofx [:db :wallet :send-transaction :asset-error]
            (i18n/label :t/changed-asset-warning {:old old-symbol :new new-symbol})))

(defn changed-amount-warning [cofx old-amount new-amount]
  (assoc-in cofx [:db :wallet :send-transaction :amount-error]
            (i18n/label :t/changed-amount-warning {:old old-amount :new new-amount})))

(defn use-default-eth-gas [cofx]
  (assoc-in cofx [:db :wallet :send-transaction :gas]
            ethereum/default-transaction-gas))

(handlers/register-handler-fx
 :wallet/fill-request-from-url
 (fn [{{:keys [network] :as db} :db} [_ data origin]]
   (let [{:keys [view-id]}                      db
         current-chain-id                       (get-in constants/default-networks [network :config :NetworkId])
         {:keys [address chain-id] :as details} (extract-details data current-chain-id)
         valid-network?                         (boolean (= current-chain-id chain-id))
         previous-state                         (get-in db [:wallet :send-transaction])
         old-symbol                             (:symbol previous-state)
         new-symbol                             (:symbol details)
         old-amount                             (:amount previous-state)
         new-amount                             (:value details)
         new-gas                                (:gas details)]
     (cond-> {:db         db
              :dispatch   [:navigate-back]}
       (and address (= :choose-recipient view-id)) (assoc :dispatch [:navigate-back])
       (and address valid-network?) (update :db #(fill-request-details % details))
       (and old-symbol new-symbol (not= old-symbol new-symbol)) (changed-asset-warning old-symbol new-symbol)
       (and old-amount new-amount (not= old-amount new-amount)) (changed-amount-warning old-amount new-amount)
       ;; NOTE(goranjovic) - the next line is there is because QR code scanning switches the amount to ETH
       ;; automatically, so we need to update the gas limit accordingly. The check for origin screen is there
       ;; so that we wouldn't also switch gas limit to ETH specific if the user pastes address as text.
       ;; We need to check if address is defined so that we wouldn't trigger this behavior when invalid QR is scanned
       ;; (e.g. whisper-id)
       (and address (= origin :qr) (not new-gas)) (use-default-eth-gas)
       (not address) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))
       (and address (not valid-network?)) (assoc :show-error (i18n/label :t/wallet-invalid-chain-id {:data data :chain current-chain-id}))))))

(handlers/register-handler-fx
 :wallet/fill-request-from-contact
 (fn [{db :db} [_ {:keys [address name whisper-identity]}]]
   {:db         (fill-request-details db {:address address :name name :whisper-identity whisper-identity})
    :dispatch   [:navigate-back]}))

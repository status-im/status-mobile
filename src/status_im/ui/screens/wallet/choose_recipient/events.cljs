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

(defn- fill-request-details [db {:keys [address name value symbol gas gasPrice]}]
  {:pre [(not (nil? address))]}
  (update-in
    db [:wallet :send-transaction]
    #(cond-> (assoc % :to address :to-name name)
             value       (assoc :amount value)
             symbol      (assoc :symbol symbol)
             gas         (assoc :gas (money/bignumber gas))
             gasPrice    (assoc :gas-price (money/bignumber gasPrice))
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

(handlers/register-handler-fx
  :wallet/fill-request-from-url
  (fn [{{:keys [network] :as db} :db} [_ data]]
    (let [{:keys [view-id]}                db
          current-chain-id                 (get-in constants/default-networks [network :raw-config :NetworkId])
          {:keys [address chain-id] :as details} (extract-details data current-chain-id)
          valid-network?                   (boolean (= current-chain-id chain-id))]
      (cond-> {:db         db
               :dispatch   [:navigate-back]}
        (and address (= :choose-recipient view-id)) (assoc :dispatch [:navigate-back])
        (and address valid-network?) (update :db #(fill-request-details % details))
        (not address) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))
        (and address (not valid-network?)) (assoc :show-error (i18n/label :t/wallet-invalid-chain-id {:data data :chain current-chain-id}))))))

(handlers/register-handler-fx
  :wallet/fill-request-from-contact
  (fn [{db :db} [_ {:keys [address name]}]]
    {:db         (fill-request-details db {:address address :name name})
     :dispatch   [:navigate-back]}))

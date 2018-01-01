(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-db
  :wallet/toggle-flashlight
  (fn [db]
    (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
          toggled-state (if (= :on flashlight-state) :off :on)]
      (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state))))

(defn- fill-request-details [db {:keys [address name amount symbol gas gas-price]}]
  (update-in
    db [:wallet :send-transaction]
    #(cond-> (assoc % :to address :to-name name)
             amount    (assoc :amount amount)
             symbol    (assoc :symbol symbol)
             gas       (assoc :gas gas)
             gas-price (assoc :gas-price gas-price)
             (and symbol (not gas-price))
             (assoc :gas-price (ethereum/estimated-gas symbol)))))

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
  (fn [{{:keys [web3 network] :as db} :db} [_ data name]]
    (let [{:keys [view-id]}                db
          current-chain-id                 (get-in constants/default-networks [network :raw-config :NetworkId])
          {:keys [address chain-id value symbol gas gas-price]} (extract-details data current-chain-id)
          valid-network?                   (boolean (= current-chain-id chain-id))]
      (cond-> {:db db}
        (and address (= :choose-recipient view-id)) (assoc :dispatch [:navigate-back])
        (and address valid-network?) (update :db #(fill-request-details % {:address   address
                                                                           :name      name
                                                                           :symbol    symbol
                                                                           :value     value
                                                                           :gas       gas
                                                                           :gas-price gas-price}))
        (not address) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))
        (and address (not valid-network?)) (assoc :show-error (i18n/label :t/wallet-invalid-chain-id {:data data}))))))

(handlers/register-handler-fx
  :wallet/fill-request-from-contact
  (fn [{db :db} [_ {:keys [address name]}]]
    {:db         (fill-request-details db {:address address :name name})
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))

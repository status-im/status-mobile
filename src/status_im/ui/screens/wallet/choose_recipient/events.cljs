(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.eip681 :as eip681]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-db
  :wallet/toggle-flashlight
  (fn [db]
    (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
          toggled-state (if (= :on flashlight-state) :off :on)]
      (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state))))

(defn choose-address-and-name [db address name amount]
  (update-in
    db [:wallet :send-transaction]
    #(cond-> (assoc % :to-address address :to-name name)
             amount (assoc :amount amount))))

(defn- extract-details
  "First try to parse as EIP681 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` and `chain-id` keys"
  [s network]
  (or (eip681/parse-uri s) {:address s :chain-id network}))

(handlers/register-handler-fx
  :choose-recipient
  (fn [{{:keys [web3 network] :as db} :db} [_ data name]]
    (let [{:keys [view-id]}                db
          current-network-id               (get-in constants/default-networks [network :raw-config :NetworkId])
          {:keys [address chain-id] :as m} (extract-details data current-network-id)
          ;; isAddress works with or without address with leading '0x'
          valid-address?                   (.isAddress web3 address)
          valid-network?                   (boolean (= current-network-id chain-id))]
      (cond-> {:db db}
        (and valid-address? (= :choose-recipient view-id)) (assoc :dispatch [:navigate-back])
        (and valid-network? valid-address?) (update :db #(choose-address-and-name % address name (eip681/parse-value m)))
        (not valid-address?) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))
        (and valid-address? (not valid-network?)) (assoc :show-error (i18n/label :t/wallet-invalid-chain-id {:data data}))))))

(handlers/register-handler-fx
  :wallet-open-send-transaction
  (fn [{db :db} [_ address name]]
    {:db         (choose-address-and-name db address name nil)
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))

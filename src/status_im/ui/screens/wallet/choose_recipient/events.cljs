(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.i18n :as i18n]
            [status-im.utils.eip.eip67 :as eip67]
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
  "First try to parse as EIP67 URI, if not assume this is an address directly.
   Returns a map containing at least the `address` key"
  [s]
  (or (eip67/parse-uri s) {:address s}))

(handlers/register-handler-fx
  :choose-recipient
  (fn [{{:keys [web3] :as db} :db} [_ data name]]
    (let [{:keys [view-id]} db
          m              (extract-details data)
          address        (:address m)
          ;; isAddress works with or without address with leading '0x'
          valid-address? (.isAddress web3 address)]
      (cond-> {:db db}
        valid-address? (update :db #(choose-address-and-name % address name (:value m)))
        (not valid-address?) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))))))

(handlers/register-handler-fx
  :wallet-open-send-transaction
  (fn [{db :db} [_ address name]]
    {:db         (choose-address-and-name db address name nil)
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))

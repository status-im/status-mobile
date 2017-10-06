(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.i18n :as i18n]
            [status-im.utils.eip.eip67 :as eip67]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-db
  :wallet/toggle-flashlight
  (fn [db]
    (let [flashlight-state (get-in db [:wallet/send-transaction :camera-flashlight])
          toggled-state (if (= :on flashlight-state) :off :on)]
      (assoc-in db [:wallet/send-transaction :camera-flashlight] toggled-state))))

(defn choose-address-and-name [db address name]
  (update db :wallet/send-transaction assoc :to-address address :to-name name))

(handlers/register-handler-fx
  :choose-recipient
  (fn [{{:keys [web3] :as db} :db} [_ data name]]
    (let [{:keys [view-id]} db
          address (:address (eip67/parse-uri data))
          valid-address? (.isAddress web3 address)]
      (cond-> {:db db}
              (= :choose-recipient view-id) (assoc :dispatch [:navigate-back])
              valid-address? (update :db #(choose-address-and-name % address name))
              (not valid-address?) (assoc :show-error (i18n/label :t/wallet-invalid-address {:data data}))))))

(handlers/register-handler-fx
  :wallet-open-send-transaction
  (fn [{db :db} [_ address name]]
    {:db         (choose-address-and-name db address name)
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))
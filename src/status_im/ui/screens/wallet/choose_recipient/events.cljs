(ns status-im.ui.screens.wallet.choose-recipient.events
  (:require [status-im.utils.handlers :as handlers]))

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
  (fn [{:keys [db]} [_ address name]]
    (let [{:keys [view-id]} db]
      (cond-> {:db (choose-address-and-name db address name)}
              (= :choose-recipient view-id) (assoc :dispatch [:navigate-back])))))

(handlers/register-handler-fx
  :wallet-open-send-transaction
  (fn [{db :db} [_ address name]]
    {:db         (choose-address-and-name db address name)
     :dispatch-n [[:navigate-back]
                  [:navigate-back]]}))
(ns status-im.ui.screens.wallet.send.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.money :as money]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.hex :as utils.hex]))

(re-frame/reg-sub ::send-transaction
                  :<- [:wallet]
                  (fn [wallet]
                    (:send-transaction wallet)))

(re-frame/reg-sub :wallet.send/symbol
                  :<- [::send-transaction]
                  (fn [send-transaction]
                    (:symbol send-transaction)))

(re-frame/reg-sub :wallet.send/advanced?
                  :<- [::send-transaction]
                  (fn [send-transaction]
                    (:advanced? send-transaction)))

(re-frame/reg-sub :wallet.send/camera-flashlight
                  :<- [::send-transaction]
                  (fn [send-transaction]
                    (:camera-flashlight send-transaction)))

(re-frame/reg-sub :wallet.send/wrong-password?
                  :<- [::send-transaction]
                  (fn [send-transaction]
                    (:wrong-password? send-transaction)))

(re-frame/reg-sub :wallet.send/sign-password-enabled?
                  :<- [::send-transaction]
                  (fn [{:keys [password]}]
                    (and (not (nil? password)) (not= password ""))))

(re-frame/reg-sub ::unsigned-transactions
                  :<- [:wallet]
                  (fn [wallet]
                    (:transactions-unsigned wallet)))

(re-frame/reg-sub ::unsigned-transaction
                  :<- [::send-transaction]
                  :<- [::unsigned-transactions]
                  (fn [[send-transaction unsigned-transactions]]
                    (when-let [unsigned-transaction (get unsigned-transactions
                                                         (:id send-transaction))]
                      (merge send-transaction
                             unsigned-transaction
                             {:gas       (or (:gas send-transaction) (:gas unsigned-transaction))
                              :gas-price (or (:gas-price send-transaction) (:gas-price unsigned-transaction))}))))

(defn edit-or-transaction-data
  "Set up edit data structure, defaulting to transaction when not available"
  [transaction edit]
  (cond-> edit
    (not (get-in edit [:gas-price :value]))
    (models.wallet/build-edit
     :gas-price
     (money/to-fixed (money/wei-> :gwei (:gas-price transaction))))

    (not (get-in edit [:gas :value]))
    (models.wallet/build-edit
     :gas
     (money/to-fixed (:gas transaction)))))

(re-frame/reg-sub :wallet/edit
                  :<- [::send-transaction]
                  :<- [::unsigned-transaction]
                  :<- [:wallet]
                  (fn [[send-transaction unsigned-transaction {:keys [edit]}]]
                    (edit-or-transaction-data
                     (if (:id send-transaction)
                       unsigned-transaction
                       send-transaction)
                     edit)))

(defn check-sufficient-funds [transaction balance symbol amount]
  (assoc transaction :sufficient-funds?
         (or (nil? amount)
             (money/sufficient-funds? amount (get balance symbol)))))

(defn check-sufficient-gas [transaction balance symbol amount]
  (assoc transaction :sufficient-gas?
         (or (nil? amount)
             (let [available-ether   (get balance :ETH)
                   available-for-gas (if (= :ETH symbol)
                                       (.minus available-ether (money/bignumber amount))
                                       available-ether)]
               (money/sufficient-funds? (-> transaction
                                            :max-fee
                                            money/bignumber
                                            (money/formatted->internal :ETH 18))
                                        (money/bignumber available-for-gas))))))

(re-frame/reg-sub :wallet.send/transaction
                  :<- [::send-transaction]
                  :<- [:balance]
                  (fn [[{:keys [amount symbol] :as transaction} balance]]
                    (-> transaction
                        (models.wallet/add-max-fee)
                        (check-sufficient-funds balance symbol amount)
                        (check-sufficient-gas balance symbol amount))))

(re-frame/reg-sub :wallet.send/unsigned-transaction
                  :<- [::unsigned-transaction]
                  :<- [:get-contacts-by-address]
                  :<- [:balance]
                  (fn [[{:keys [value to symbol] :as transaction} contacts balance]]
                    (when transaction
                      (let [contact (contacts (utils.hex/normalize-hex to))]
                        (-> transaction
                            (assoc :amount  value
                                   :to-name (:name contact))
                            (models.wallet/add-max-fee)
                            (check-sufficient-funds balance symbol value)
                            (check-sufficient-gas balance symbol value))))))

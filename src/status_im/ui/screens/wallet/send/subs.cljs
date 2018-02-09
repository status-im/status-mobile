(ns status-im.ui.screens.wallet.send.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.money :as money]
            [status-im.utils.hex :as utils.hex]))

(re-frame/reg-sub :wallet.sent/close-transaction-screen-event
  :<- [:get :navigation-stack]
  (fn [navigation-stack]
    (case (second navigation-stack)
      :wallet-send-transaction [:navigate-to-clean :wallet]
      [:navigate-back])))

(re-frame/reg-sub ::send-transaction
  :<- [:wallet]
  (fn [wallet]
    (let [transaction (:send-transaction wallet)]
      ;NOTE(goranjovic): the transactions started from chat using /send command
      ; are only in ether, so this parameter defaults to ETH
      (if (:symbol transaction)
        transaction
        (assoc transaction :symbol :ETH)))))


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
             unsigned-transaction))))

(re-frame/reg-sub :wallet/edit
  :<- [:wallet]
  :edit)

(defn sign-enabled? [amount-error to amount]
  (and
   (nil? amount-error)
   (not (nil? to)) (not= to "")
   (not (nil? amount)) (not= amount "")))

(re-frame/reg-sub :wallet.send/transaction
  :<- [::send-transaction]
  :<- [:balance]
  (fn [[{:keys [amount symbol] :as transaction} balance]]
    (assoc transaction :sufficient-funds? (or (nil? amount)
                                              (money/sufficient-funds? amount (get balance symbol))))))

(re-frame/reg-sub :wallet.send/unsigned-transaction
  :<- [::unsigned-transaction]
  :<- [:contacts/by-address]
  :<- [:balance]
  (fn [[{:keys [value to symbol] :as transaction} contacts balance]]
    (when transaction
      (let [contact           (contacts (utils.hex/normalize-hex to))
            sufficient-funds? (money/sufficient-funds? value (get balance symbol))]
        (cond-> (assoc transaction
                       :amount value
                       :sufficient-funds? sufficient-funds?)
          contact                 (assoc :to-name (:name contact)))))))

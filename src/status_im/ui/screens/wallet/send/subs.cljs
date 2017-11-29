(ns status-im.ui.screens.wallet.send.subs
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
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
    (:send-transaction wallet)))

(re-frame/reg-sub :wallet.send/camera-dimensions
  :<- [::send-transaction]
  (fn [send-transaction]
    (:camera-dimensions send-transaction)))

(re-frame/reg-sub :wallet.send/camera-flashlight
  :<- [::send-transaction]
  (fn [send-transaction]
    (:camera-flashlight send-transaction)))

(re-frame/reg-sub :wallet.send/camera-permitted?
  :<- [::send-transaction]
  (fn [send-transaction]
    (:camera-permitted? send-transaction)))

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

(defn sign-enabled? [amount-error to-address amount]
  (and
   (nil? amount-error)
   (not (nil? to-address)) (not= to-address "")
   (not (nil? amount)) (not= amount "")))

(re-frame/reg-sub :wallet.send/transaction
  :<- [::send-transaction]
  :<- [:balance]
  (fn [[{:keys [amount to] :as transaction} balance]]
    (assoc transaction :sufficient-funds? (or (nil? amount)
                                              ;; TODO(jeluard) Modify to consider tokens
                                              (money/sufficient-funds? amount (get balance :ETH))))))

(re-frame/reg-sub :wallet.send/unsigned-transaction
  :<- [::unsigned-transaction]
  :<- [:contacts/by-address]
  :<- [:balance]
  (fn [[{:keys [value to] :as transaction} contacts balance]]
    (when transaction
      (let [contact           (contacts (utils.hex/normalize-hex to))
            ;; TODO(jeluard) Modify to consider tokens
            sufficient-funds? (money/sufficient-funds? value (get balance :ETH))]
        (cond-> (assoc transaction
                       :amount value
                       :sufficient-funds? sufficient-funds?)
          contact                 (assoc :to-name (:name contact)))))))

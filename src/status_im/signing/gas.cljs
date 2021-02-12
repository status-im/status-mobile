(ns status-im.signing.gas
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]))

(def min-gas-price-wei ^js (money/bignumber 1))

(def min-gas-units ^js (money/bignumber 21000))

(defmulti get-error-label-key (fn [type _] type))

(defmethod get-error-label-key :gasPrice [_ value]
  (cond
    (not value) :t/invalid-number
    (.lt ^js (money/->wei :gwei value) min-gas-price-wei) :t/wallet-send-min-wei
    (-> (money/->wei :gwei value) .decimalPlaces pos?) :t/invalid-number))

(defmethod get-error-label-key :gas [_ ^js value]
  (cond
    (not value) :t/invalid-number
    (.lt value min-gas-units) :t/wallet-send-min-units
    (-> value .decimalPlaces pos?) :t/invalid-number))

(defmethod get-error-label-key :default [_ value]
  (when (or (not value)
            (<= value 0))
    :t/invalid-number))

(defn calculate-max-fee
  [^js gas ^js gasPrice]
  (if (and gas gasPrice)
    (money/to-fixed (money/wei->ether (.times gas gasPrice)))
    "0"))

(defn edit-max-fee [edit]
  (let [gasPrice (get-in edit [:gasPrice :value-number])
        gas      (get-in edit [:gas :value-number])]
    (assoc edit :max-fee (calculate-max-fee gas gasPrice))))

(defn build-edit
  "Takes the previous edit, either :gas or :gas-price and a value as string.
  Wei for gas, and gwei for gas price.
  Validates them and sets max fee"
  [edit-value key value]
  (let [^js bn-value        (money/bignumber value)
        error-label-key (get-error-label-key key bn-value)
        data            (if error-label-key
                          {:value   value
                           :max-fee 0
                           :error   (i18n/label error-label-key)}
                          {:value        value
                           :value-number (if (= :gasPrice key)
                                           (money/->wei :gwei bn-value)
                                           bn-value)})]
    (-> edit-value
        (assoc key data)
        edit-max-fee)))

(fx/defn edit-value
  {:events [:signing.edit-fee.ui/edit-value]}
  [{:keys [db]} key value]
  {:db (update db :signing/edit-fee build-edit key value)})

(fx/defn update-estimated-gas-success
  {:events [:signing/update-estimated-gas-success]}
  [{db :db} gas]
  {:db (-> db
           (assoc-in [:signing/tx :gas] gas)
           (assoc-in [:signing/edit-fee :gas-loading?] false))})

(fx/defn update-gas-price-success
  {:events [:signing/update-gas-price-success]}
  [{db :db} price]
  {:db (-> db
           (assoc-in [:signing/tx :gasPrice] price)
           (assoc-in [:signing/edit-fee :gas-price-loading?] false))})

(fx/defn update-estimated-gas-error
  {:events [:signing/update-estimated-gas-error]}
  [{db :db} {:keys [message]}]
  {:db (-> db
           (assoc-in [:signing/edit-fee :gas-loading?] false)
           (assoc-in [:signing/tx :gas-error-message] message))})

(fx/defn update-gas-price-error
  {:events [:signing/update-gas-price-error]}
  [{db :db}]
  {:db (assoc-in db [:signing/edit-fee :gas-price-loading?] false)})

(fx/defn open-fee-sheet
  {:events [:signing.ui/open-fee-sheet]}
  [{{:signing/keys [tx] :as db} :db :as cofx} sheet-opts]
  (let [{:keys [gas gasPrice]} tx
        edit-fee (-> {}
                     (build-edit :gas (money/to-fixed gas))
                     (build-edit :gasPrice (money/to-fixed (money/wei-> :gwei gasPrice))))]
    (fx/merge cofx
              {:db (assoc db :signing/edit-fee edit-fee)}
              (bottom-sheet/show-bottom-sheet {:view sheet-opts}))))

(fx/defn submit-fee
  {:events [:signing.edit-fee.ui/submit]}
  [{{:signing/keys [edit-fee] :as db} :db :as cofx}]
  (let [{:keys [gas gasPrice]} edit-fee]
    (fx/merge cofx
              {:db (update db :signing/tx assoc :gas (:value-number gas) :gasPrice (:value-number gasPrice))}
              (bottom-sheet/hide-bottom-sheet))))

(re-frame/reg-fx
 :signing/update-gas-price
 (fn [{:keys [success-event error-event]}]
   (json-rpc/call
    {:method     "eth_gasPrice"
     :on-success #(re-frame/dispatch [success-event %])
     :on-error #(re-frame/dispatch [error-event %])})))

(re-frame/reg-fx
 :signing/update-estimated-gas
 (fn [{:keys [obj success-event error-event]}]
   (json-rpc/call
    {:method     "eth_estimateGas"
     :params     [obj]
     :on-success #(re-frame/dispatch [success-event %])
     :on-error #(re-frame/dispatch [error-event %])})))

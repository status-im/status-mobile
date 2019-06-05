(ns status-im.signing.gas
  (:require [status-im.utils.money :as money]
            [status-im.utils.fx :as fx]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
            [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]))

(def min-gas-price-wei (money/bignumber 1))

(defmulti get-error-label-key (fn [type _] type))

(defmethod get-error-label-key :gasPrice [_ value]
  (cond
    (not value) :t/invalid-number
    (.lt (money/->wei :gwei value) min-gas-price-wei) :t/wallet-send-min-wei
    (-> (money/->wei :gwei value) .decimalPlaces pos?) :t/invalid-number))

(defmethod get-error-label-key :default [_ value]
  (when (or (not value)
            (<= value 0))
    :t/invalid-number))

(defn calculate-max-fee
  [gas gasPrice]
  (if (and gas gasPrice)
    (money/to-fixed (money/wei->ether (.times gas gasPrice)))
    "0"))

(defn edit-max-fee [edit]
  (let [gasPrice (get-in edit [:gasPrice :value-number])
        gas      (get-in edit [:gas :value-number])]
    (assoc edit :max-fee (calculate-max-fee gas gasPrice))))

(defn build-edit [edit-value key value]
  "Takes the previous edit, either :gas or :gas-price and a value as string.
  Wei for gas, and gwei for gas price.
  Validates them and sets max fee"
  (let [bn-value        (money/bignumber value)
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
  {:db (assoc-in db [:signing/tx :gas] gas)})

(fx/defn update-gas-price-success
  {:events [:signing/update-gas-price-success]}
  [{db :db} price]
  {:db (assoc-in db [:signing/tx :gasPrice] price)})

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
 (fn [{:keys [success-event edit?]}]
   (json-rpc/call
    {:method     "eth_gasPrice"
     :on-success #(re-frame/dispatch [success-event % edit?])})))

(re-frame/reg-fx
 :signing/update-estimated-gas
 (fn [{:keys [obj success-event]}]
   (json-rpc/call
    {:method     "eth_estimateGas"
     :params     [obj]
     :on-success #(re-frame/dispatch [success-event %])})))
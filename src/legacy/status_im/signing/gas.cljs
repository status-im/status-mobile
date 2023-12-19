(ns legacy.status-im.signing.gas
  (:require
    [clojure.string :as string]
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.popover.core :as popover]
    [legacy.status-im.signing.eip1559 :as eip1559]
    [re-frame.core :as re-frame]
    [status-im2.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(def min-gas-price-wei ^js (money/bignumber 1))

(def min-gas-units ^js (money/bignumber 21000))

(defmulti get-error-label-key (fn [type _] type))

(defmethod get-error-label-key :gasPrice
  [_ value]
  (cond
    (not value)                                           :t/invalid-number
    (.lt ^js (money/->wei :gwei value) min-gas-price-wei) :t/wallet-send-min-wei
    (-> (money/->wei :gwei value) .decimalPlaces pos?)    :t/invalid-number))

(defmethod get-error-label-key :gas
  [_ ^js value]
  (cond
    (not value)                    :t/invalid-number
    (.lt value min-gas-units)      :t/wallet-send-min-units
    (-> value .decimalPlaces pos?) :t/invalid-number))

(defmethod get-error-label-key :default
  [_ value]
  (when (or (not value)
            (<= value 0))
    :t/invalid-number))

(defn calculate-max-fee
  [^js gas ^js gasPrice]
  (if (and gas gasPrice)
    (money/to-fixed (money/wei->ether (.times gas gasPrice)))
    "0"))

(defn edit-max-fee
  [edit]
  (let [gasPrice (get-in edit [:gasPrice :value-number])
        gas      (get-in edit [:gas :value-number])]
    (assoc edit :max-fee (calculate-max-fee gas gasPrice))))

(defn build-edit
  "Takes the previous edit, either :gas or :gas-price and a value as string.
  Wei for gas, and gwei for gas price.
  Validates them and sets max fee"
  [edit-value k value]
  (let [^js bn-value    (money/bignumber value)
        error-label-key (get-error-label-key k bn-value)
        data            (if error-label-key
                          {:value   value
                           :max-fee 0
                           :error   (i18n/label error-label-key)}
                          {:value        value
                           :value-number (if (= :gasPrice k)
                                           (money/->wei :gwei bn-value)
                                           bn-value)})]
    (-> edit-value
        (assoc k data)
        edit-max-fee)))

;; TODO(rasom): this number is almost arbitrary, I was able to sent txs with
;; 0.2 gwei tip, so it should be revisited lately
(def minimum-priority-fee-gwei
  (money/bignumber 0.3))

(defn get-suggested-tip
  [latest-priority-fee]
  (money/bignumber latest-priority-fee))

(defn get-minimum-priority-fee
  [latest-priority-fee]
  (let [latest-priority-fee-bn (money/bignumber latest-priority-fee)
        suggested-tip-gwei     (money/wei->gwei (get-suggested-tip latest-priority-fee-bn))]
    (if (money/greater-than minimum-priority-fee-gwei suggested-tip-gwei)
      (money/div suggested-tip-gwei 2)
      minimum-priority-fee-gwei)))

(defn get-suggestions-range
  [latest-priority-fee]
  (let [current-minimum-fee (get-minimum-priority-fee latest-priority-fee)]
    [(if (money/greater-than minimum-priority-fee-gwei current-minimum-fee)
       current-minimum-fee
       minimum-priority-fee-gwei)
     (money/wei->gwei (money/bignumber latest-priority-fee))]))

(def average-priority-fee
  (money/wei->gwei (money/->wei :gwei 1.5)))

(defn validate-max-fee
  [db]
  (let [{:keys [maxFeePerGas maxPriorityFeePerGas]} (get db :signing/edit-fee)
        latest-base-fee                             (money/wei->gwei
                                                     (money/bignumber
                                                      (get db :wallet-legacy/current-base-fee)))
        fee-error                                   (cond
                                                      (or (:error maxFeePerGas)
                                                          (:error maxPriorityFeePerGas))
                                                      nil

                                                      (money/greater-than latest-base-fee
                                                                          (:value-number maxFeePerGas))
                                                      {:label    (i18n/label :t/below-base-fee)
                                                       :severity :error})]
    (if fee-error
      (assoc-in db [:signing/edit-fee :maxFeePerGas :fee-error] fee-error)
      (update-in db [:signing/edit-fee :maxFeePerGas] dissoc :fee-error))))

(defn validate-max-priority-fee
  [db]
  (let [{:keys [maxPriorityFeePerGas]} (get db :signing/edit-fee)
        latest-priority-fee            (get db :wallet-legacy/current-priority-fee)
        fee-error                      (cond
                                         (:error maxPriorityFeePerGas)
                                         nil

                                         (money/greater-than (get-minimum-priority-fee
                                                              (money/div
                                                               (money/wei->gwei (money/bignumber
                                                                                 latest-priority-fee))
                                                               2))
                                                             (:value-number maxPriorityFeePerGas))
                                         {:label    (i18n/label :t/low-tip)
                                          :severity :error}

                                         #_(money/greater-than average-priority-fee
                                                               (:value-number maxPriorityFeePerGas))
                                         #_{:label    (i18n/label :t/lower-than-average-tip)
                                            :severity :error})]
    (if fee-error
      (assoc-in db [:signing/edit-fee :maxPriorityFeePerGas :fee-error] fee-error)
      (update-in db [:signing/edit-fee :maxPriorityFeePerGas] dissoc :fee-error))))

(defn validate-eip1559-fees
  [db]
  (if (eip1559/sync-enabled?)
    (reduce
     (fn [acc f]
       (f acc))
     db
     [validate-max-fee
      validate-max-priority-fee])
    db))

(rf/defn edit-value
  {:events [:signing.edit-fee.ui/edit-value]}
  [{:keys [db]} key value]
  {:db (-> db
           (assoc-in [:signing/edit-fee :selected-fee-option] :custom)
           (update :signing/edit-fee build-edit key value)
           validate-eip1559-fees)})

(defn get-fee-options
  [tip slow normal fast]
  (let [tip-bn    (money/bignumber tip)
        normal-bn (money/bignumber normal)
        slow-bn   (money/bignumber slow)
        fast-bn   (money/bignumber fast)]
    {:normal
     {:base-fee normal-bn
      :tip      tip-bn
      :fee      (money/add normal-bn tip-bn)}
     :slow
     {:base-fee slow-bn
      :tip      tip-bn
      :fee      (money/add slow-bn tip-bn)}
     :fast
     {:base-fee fast-bn
      :tip      tip-bn
      :fee      (money/add fast-bn tip-bn)}}))

(rf/defn set-fee-option
  {:events [:signing.edit-fee.ui/set-option]}
  [{:keys [db] :as cofx} option]
  (let [tip               (get db :wallet-legacy/current-priority-fee)
        slow              (get db :wallet-legacy/slow-base-fee)
        normal            (get db :wallet-legacy/normal-base-fee)
        fast              (get db :wallet-legacy/fast-base-fee)
        {:keys [fee tip]} (get (get-fee-options tip slow normal fast) option)]
    {:db (-> db
             (assoc-in [:signing/edit-fee :selected-fee-option] option)
             (update :signing/edit-fee
                     build-edit
                     :maxFeePerGas (money/wei->gwei fee))
             (update :signing/edit-fee
                     build-edit
                     :maxPriorityFeePerGas (money/wei->gwei tip)))}))

(rf/defn set-priority-fee
  {:events [:signing.edit-fee.ui/set-priority-fee]}
  [{:keys [db]} value]
  (let [{:keys [maxFeePerGas maxPriorityFeePerGas]}
        (get db :signing/edit-fee)
        latest-base-fee (get db :wallet-legacy/current-base-fee)
        max-fee-value (:value-number maxFeePerGas)
        max-priority-fee-value (:value-number maxPriorityFeePerGas)
        new-value (money/bignumber value)
        fee-without-tip (money/sub max-fee-value max-priority-fee-value)
        base-fee (money/wei->gwei (money/bignumber latest-base-fee))
        new-max-fee-value
        (money/to-fixed
         (if (money/greater-than base-fee fee-without-tip)
           (money/add new-value base-fee)
           (money/add new-value fee-without-tip)))]
    {:db (-> db
             (update :signing/edit-fee build-edit :maxPriorityFeePerGas value)
             (update :signing/edit-fee build-edit :maxFeePerGas new-max-fee-value)
             validate-eip1559-fees)}))

(rf/defn update-estimated-gas-success
  {:events [:signing/update-estimated-gas-success]}
  [{db :db} gas]
  (when (contains? db :signing/tx)
    {:db (-> db
             (assoc-in [:signing/tx :gas] gas)
             (assoc-in [:signing/edit-fee :gas-loading?] false))}))

(rf/defn update-gas-price-success
  {:events [:signing/update-gas-price-success]}
  [{db :db} price]
  (if (eip1559/sync-enabled?)
    (let [{:keys [normal-base-fee max-priority-fee]} price
          max-priority-fee-bn                        (money/with-precision (get-suggested-tip
                                                                            max-priority-fee)
                                                                           0)]
      {:db (-> db
               (assoc-in [:signing/tx :maxFeePerGas]
                         (money/to-hex (money/add max-priority-fee-bn
                                                  (money/bignumber normal-base-fee))))
               (assoc-in [:signing/tx :maxPriorityFeePerGas]
                         (money/to-hex max-priority-fee-bn))
               (assoc-in [:signing/edit-fee :gas-price-loading?] false))})
    {:db (-> db
             (assoc-in [:signing/tx :gasPrice] price)
             (assoc-in [:signing/edit-fee :gas-price-loading?] false))}))

(rf/defn update-estimated-gas-error
  {:events [:signing/update-estimated-gas-error]}
  [{db :db} {:keys [message]}]
  (log/warn "signing/update-estimated-gas-error" message)
  {:db (-> db
           (assoc-in [:signing/edit-fee :gas-loading?] false)
           (assoc-in [:signing/tx :gas-error-message] message))})

(rf/defn update-gas-price-error
  {:events [:signing/update-gas-price-error]}
  [{db :db}]
  {:db (assoc-in db [:signing/edit-fee :gas-price-loading?] false)})

(rf/defn open-fee-sheet
  {:events [:signing.ui/open-fee-sheet]}
  [{{:signing/keys [tx] :as db} :db :as cofx} sheet-opts]
  (let [{:keys [gas gasPrice maxFeePerGas maxPriorityFeePerGas]} tx
        max-fee                                                  (money/to-fixed (money/wei->gwei
                                                                                  maxFeePerGas))
        max-priority-fee                                         (money/to-fixed (money/wei->gwei
                                                                                  maxPriorityFeePerGas))
        edit-fee                                                 (reduce
                                                                  (partial apply build-edit)
                                                                  {:selected-fee-option
                                                                   (get-in db
                                                                           [:signing/edit-fee
                                                                            :selected-fee-option])}
                                                                  {:gas (money/to-fixed gas)
                                                                   :gasPrice (money/to-fixed
                                                                              (money/wei->gwei gasPrice))
                                                                   :maxFeePerGas max-fee
                                                                   :maxPriorityFeePerGas
                                                                   max-priority-fee})]
    (rf/merge cofx
              {:db (assoc db :signing/edit-fee edit-fee)}
              (bottom-sheet/show-bottom-sheet-old {:view sheet-opts}))))

(rf/defn submit-fee
  {:events [:signing.edit-fee.ui/submit]}
  [{{:signing/keys [edit-fee] :as db} :db :as cofx} force?]
  (let [{:keys [gas gasPrice maxFeePerGas maxPriorityFeePerGas]} edit-fee
        errors?
        (keep
         (fn [[k {:keys [fee-error]}]]
           (when (= :error (:severity fee-error))
             [k fee-error]))
         edit-fee)]
    (if (and (seq errors?)
             (not force?))
      (popover/show-popover cofx {:view :fees-warning})
      (rf/merge cofx
                {:db (update db
                             :signing/tx           assoc
                             :gas                  (:value-number gas)
                             :gasPrice             (:value-number gasPrice)
                             :maxFeePerGas         (money/->wei :gwei (:value-number maxFeePerGas))
                             :maxPriorityFeePerGas (money/->wei :gwei
                                                                (:value-number maxPriorityFeePerGas)))}
                (bottom-sheet/hide-bottom-sheet-old)))))

(rf/defn submit-nonce
  {:events [:signing.nonce/submit]}
  [{db :db :as cofx} nonce]
  (rf/merge cofx
            {:db (assoc-in db [:signing/tx :nonce] (if (string/blank? nonce) nil nonce))}
            (bottom-sheet/hide-bottom-sheet-old)))

(re-frame/reg-fx
 :signing/update-gas-price
 (fn [{:keys [success-callback error-callback network-id] :as params}]
   (eip1559/enabled?
    network-id
    (fn []
      (json-rpc/call
       {:method     "eth_feeHistory"
        ;; NOTE(rasom): We don't need `reward` atm but if the last parameter is
        ;; `nil` request fails on some chains (particularly on xDai).
        :params     [101 "latest" [100]]
        :on-success #(re-frame/dispatch [::header-fetched
                                         (assoc params :fee-history %)])}))
    (fn []
      (json-rpc/call
       {:method     "eth_gasPrice"
        :on-success #(success-callback (money/bignumber %))
        :on-error   (or error-callback #(log/warn "eth_gasPrice error" %))})))))

(def london-block-gas-limit (money/bignumber 30000000))

(defn calc-percentiles
  [v ps]
  (let [sorted-v (sort-by
                  identity
                  (fn [a b]
                    (utils.money/greater-than b a))
                  v)]
    (reduce
     (fn [acc p]
       (assoc acc p (nth sorted-v p)))
     {}
     ps)))

;; It is very unlikely to be that small on mainnet, but on testnets current base
;; fee might be very small and using this value might slow transaction
(def minimum-base-fee (money/->wei :gwei (money/bignumber 1)))

(defn recommended-base-fee
  [current perc20]
  (let [fee
        (cond (money/greater-than-or-equals current perc20)
              current

              (money/greater-than perc20 current)
              perc20)]
    (if (money/greater-than minimum-base-fee fee)
      minimum-base-fee
      fee)))

(defn slow-base-fee [_ perc10] perc10)

(defn fast-base-fee
  [current]
  (let [fee (money/mul current 2)]
    (if (money/greater-than minimum-base-fee fee)
      (money/mul minimum-base-fee 2)
      fee)))

(defn check-base-fee
  [{:keys [baseFeePerGas testnet?]}]
  (let [all-base-fees    (mapv money/bignumber baseFeePerGas)
        next-base-fee    (peek all-base-fees)
        previous-fees    (subvec all-base-fees 0 101)
        current-base-fee (peek previous-fees)
        percentiles      (calc-percentiles previous-fees [10 20 80])]
    {:normal-base-fee  (money/to-hex
                        (if testnet?
                          (fast-base-fee next-base-fee)
                          (recommended-base-fee
                           next-base-fee
                           (get percentiles 20))))
     :slow-base-fee    (money/to-hex
                        (slow-base-fee
                         next-base-fee
                         (get percentiles 10)))
     :fast-base-fee    (money/to-hex (fast-base-fee next-base-fee))
     :current-base-fee (money/to-hex current-base-fee)}))

(defn max-priority-fee-hex
  [gas-price base-fee]
  (money/to-hex
   (money/sub (money/bignumber gas-price)
              (money/bignumber base-fee))))

(rf/defn header-fetched
  {:events [::header-fetched]}
  [{{:networks/keys [current-network networks]} :db}
   {:keys [error-callback success-callback fee-history] :as params}]
  {:json-rpc/call
   [;; NOTE(rasom): eth_maxPriorityFeePerGas is not supported by some networks
    ;; so it is more reliable to calculate maxPriorityFeePerGas using the value
    ;; returned by eth_gasPrice and current base fee.
    {:method     "eth_gasPrice"
     :on-success #(success-callback
                   (let [{:keys [current-base-fee] :as base-fees}
                         (check-base-fee
                          (assoc fee-history
                                 :testnet?
                                 (chain/testnet?
                                  (get-in networks [current-network :config :NetworkId]))))]
                     (merge {:max-priority-fee
                             (max-priority-fee-hex (money/bignumber %) current-base-fee)}
                            base-fees)))
     :on-error   (if error-callback
                   #(error-callback %)
                   #(log/error "Can't fetch header" %))}]})

(re-frame/reg-fx
 :signing/update-estimated-gas
 (fn [{:keys [obj success-event error-event]}]
   (json-rpc/call
    {:method     "eth_estimateGas"
     :params     [obj]
     :on-success #(re-frame/dispatch [success-event
                                      (money/bignumber (if (= (int %) 21000) % (int (* % 1.2))))])
     :on-error   #(re-frame/dispatch [error-event %])})))

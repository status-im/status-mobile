(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.send :as send-utils]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
    [utils.address :as address]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- make-limit-label
  [{:keys [amount currency]}]
  (str amount
       " "
       (some-> currency
               name
               string/upper-case)))

(def not-digits-or-dot-pattern
  #"[^0-9+\.]")

(def dot ".")

(defn valid-input?
  [current v]
  (let [max-length          12
        length-overflow?    (>= (count current) max-length)
        extra-dot?          (and (= v dot) (string/includes? current dot))
        extra-leading-zero? (and (= current "0") (= "0" (str v)))
        non-numeric?        (re-find not-digits-or-dot-pattern (str v))]
    (not (or non-numeric? extra-dot? extra-leading-zero? length-overflow?))))

(defn- add-char-to-string
  [s c idx]
  (let [size (count s)]
    (if (= size idx)
      (str s c)
      (str (subs s 0 idx)
           c
           (subs s idx size)))))

(defn- move-input-cursor
  ([input-selection-atom new-idx]
   (move-input-cursor input-selection-atom new-idx new-idx))
  ([input-selection-atom new-start-idx new-end-idx]
   (let [start-idx (if (< new-start-idx 0) 0 new-start-idx)
         end-idx   (if (< new-end-idx 0) 0 new-start-idx)]
     (swap! input-selection-atom assoc :start start-idx :end end-idx))))

(defn- normalize-input
  [current v input-selection-atom]
  (let [{:keys [start end]} @input-selection-atom]
    (if (= start end)
      (cond
        (and (string/blank? current) (= v dot))
        (do
          (move-input-cursor input-selection-atom 2)
          (str "0" v))

        (and (= current "0") (not= v dot))
        (do
          (move-input-cursor input-selection-atom 1)
          (str v))

        :else
        (do
          (move-input-cursor input-selection-atom (inc start))
          (add-char-to-string current v start)))
      current)))

(defn- make-new-input
  [current v input-selection-atom]
  (if (valid-input? current v)
    (normalize-input current v input-selection-atom)
    current))

(defn- reset-input-error
  [new-value prev-value input-error]
  (reset! input-error
    (> new-value prev-value)))

(defn delete-from-string
  [s idx]
  (let [size (count s)]
    (str (subs s 0 (dec idx)) (subs s idx size))))

(defn- estimated-fees
  [{:keys [loading-suggested-routes? fees amount receiver]}]
  [rn/view {:style style/estimated-fees-container}
   [rn/view {:style style/estimated-fees-content-container}
    [quo/button
     {:icon-only?          true
      :type                :outline
      :size                32
      :inner-style         {:opacity 1}
      :accessibility-label :advanced-button
      :disabled?           loading-suggested-routes?
      :on-press            #(js/alert "Not implemented yet")}
     :i/advanced]]
   [quo/data-item
    {:container-style style/fees-data-item
     :label           :none
     :status          (if loading-suggested-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/fees)
     :subtitle        fees}]
   [quo/data-item
    {:container-style style/amount-data-item
     :label           :none
     :status          (if loading-suggested-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/user-gets {:name receiver})
     :subtitle        amount}]])

(defn select-asset-bottom-sheet
  [clear-input!]
  (let [{preselected-token-symbol :symbol} (rf/sub [:wallet/wallet-send-token])]
    [:<> ;; Need to be a `:<>` to keep `asset-list` scrollable.
     [quo/drawer-top
      {:title           (i18n/label :t/select-asset)
       :container-style {:padding-bottom 8}}]
     [asset-list/view
      {:content-container-style  {:padding-horizontal 8
                                  :padding-bottom     8}
       :preselected-token-symbol preselected-token-symbol
       :on-token-press           (fn [token]
                                   (rf/dispatch [:wallet/edit-token-to-send token])
                                   (clear-input!))}]]))

(defn view
  ;; crypto-decimals, limit-crypto and initial-crypto-currency? args are needed
  ;; for component tests only
  [{default-on-confirm       :on-confirm
    default-limit-crypto     :limit-crypto
    default-crypto-decimals  :crypto-decimals
    on-navigate-back         :on-navigate-back
    button-one-label         :button-one-label
    button-one-props         :button-one-props
    current-screen-id        :current-screen-id
    initial-crypto-currency? :initial-crypto-currency?
    :or                      {initial-crypto-currency? true}}]
  (let [_ (rn/dismiss-keyboard!)
        bottom                (safe-area/get-bottom)
        input-value           (reagent/atom "")
        clear-input!          #(reset! input-value "")
        input-error           (reagent/atom false)
        crypto-currency?      (reagent/atom initial-crypto-currency?)
        input-selection       (reagent/atom {:start 0 :end 0})
        handle-swap           (fn [{:keys [crypto? limit-fiat limit-crypto]}]
                                (let [num-value     (parse-double @input-value)
                                      current-limit (if crypto? limit-crypto limit-fiat)]
                                  (reset! crypto-currency? crypto?)
                                  (reset-input-error num-value current-limit input-error)))
        handle-keyboard-press (fn [v loading-routes? current-limit-amount]
                                (when-not loading-routes?
                                  (let [current-value @input-value
                                        new-value     (make-new-input current-value v input-selection)
                                        num-value     (or (parse-double new-value) 0)]
                                    (reset! input-value new-value)
                                    (reset-input-error num-value current-limit-amount input-error)
                                    (reagent/flush))))
        handle-delete         (fn [loading-routes? current-limit-amount]
                                (when-not loading-routes?
                                  (let [{:keys [start end]} @input-selection
                                        new-value           (delete-from-string @input-value start)]
                                    (when (= start end)
                                      (reset-input-error new-value current-limit-amount input-error)
                                      (swap! input-value delete-from-string start)
                                      (move-input-cursor input-selection (dec start)))
                                    (reagent/flush))))
        on-long-press-delete  (fn [loading-routes?]
                                (when-not loading-routes?
                                  (reset! input-value "")
                                  (reset! input-error false)
                                  (move-input-cursor input-selection 0)
                                  (reagent/flush)))
        handle-on-change      (fn [v current-limit-amount]
                                (when (valid-input? @input-value v)
                                  (let [num-value (or (parse-double v) 0)]
                                    (reset! input-value v)
                                    (reset-input-error num-value current-limit-amount input-error)
                                    (reagent/flush))))
        on-navigate-back      on-navigate-back
        fetch-routes          (fn [input-num-value current-limit-amount bounce-duration-ms]
                                (let [nav-current-screen-id (rf/sub [:view-id])
                                      input-num-value       (or input-num-value 0)]
                                  ; this check is to prevent effect being triggered when screen is
                                  ; loaded but not being shown to the user (deep in the navigation
                                  ; stack) and avoid undesired behaviors
                                  (when (= nav-current-screen-id current-screen-id)
                                    (if-not (or (empty? @input-value)
                                                (<= input-num-value 0)
                                                (> input-num-value current-limit-amount))
                                      (debounce/debounce-and-dispatch
                                       [:wallet/get-suggested-routes {:amount @input-value}]
                                       bounce-duration-ms)
                                      (rf/dispatch [:wallet/clean-suggested-routes])))))
        handle-on-confirm     (fn []
                                (rf/dispatch [:wallet/send-select-amount
                                              {:amount   @input-value
                                               :stack-id current-screen-id}]))
        selection-change      (fn [selection]
                                ;; `reagent/flush` is needed to properly propagate the
                                ;; input cursor state. Since this is a controlled
                                ;; component the cursor will become static if
                                ;; `reagent/flush` is removed.
                                (reset! input-selection selection)
                                (reagent/flush))]
    (fn []
      (let [{fiat-currency :currency}  (rf/sub [:profile/profile])
            {token-symbol   :symbol
             token-networks :networks} (rf/sub [:wallet/wallet-send-token])
            {token-balance :total-balance
             token-balances-per-chain :balances-per-chain
             :as
             token}                    (rf/sub
                                        [:wallet/current-viewing-account-tokens-filtered
                                         (str token-symbol)])
            conversion-rate            (-> token :market-values-per-currency :usd :price)
            loading-routes?            (rf/sub
                                        [:wallet/wallet-send-loading-suggested-routes?])
            suggested-routes           (rf/sub [:wallet/wallet-send-suggested-routes])
            best-routes                (when suggested-routes
                                         (or (:best suggested-routes) []))
            route                      (rf/sub [:wallet/wallet-send-route])
            to-address                 (rf/sub [:wallet/wallet-send-to-address])
            disabled-from-chain-ids    (rf/sub
                                        [:wallet/wallet-send-disabled-from-chain-ids])
            from-values-by-chain       (rf/sub [:wallet/wallet-send-from-values-by-chain])
            to-values-by-chain         (rf/sub [:wallet/wallet-send-to-values-by-chain])
            on-confirm                 (or default-on-confirm handle-on-confirm)
            crypto-decimals            (or default-crypto-decimals
                                           (utils/get-crypto-decimals-count token))
            crypto-limit               (or default-limit-crypto
                                           (utils/get-standard-crypto-format
                                            token
                                            token-balance))
            fiat-limit                 (.toFixed (* token-balance conversion-rate) 2)
            current-limit              #(if @crypto-currency? crypto-limit fiat-limit)
            current-currency           (if @crypto-currency? token-symbol fiat-currency)
            limit-label                (make-limit-label {:amount   (current-limit)
                                                          :currency current-currency})
            input-num-value            (parse-double @input-value)
            confirm-disabled?          (or (nil? route)
                                           (empty? route)
                                           (empty? @input-value)
                                           (<= input-num-value 0)
                                           (> input-num-value (current-limit)))
            amount-text                (str @input-value " " token-symbol)
            first-route                (first route)
            native-currency-symbol     (when-not confirm-disabled?
                                         (get-in first-route [:from :native-currency-symbol]))
            native-token               (when native-currency-symbol
                                         (rf/sub [:wallet/token-by-symbol
                                                  native-currency-symbol]))
            fee-in-native-token        (when-not confirm-disabled?
                                         (send-utils/calculate-full-route-gas-fee route))
            fee-in-crypto-formatted    (when fee-in-native-token
                                         (utils/get-standard-crypto-format
                                          native-token
                                          fee-in-native-token))
            fee-in-fiat                (when-not confirm-disabled?
                                         (utils/calculate-token-fiat-value
                                          {:currency fiat-currency
                                           :balance  fee-in-native-token
                                           :token    native-token}))
            currency-symbol            (rf/sub [:profile/currency-symbol])
            fee-formatted              (when fee-in-fiat
                                         (utils/get-standard-fiat-format
                                          fee-in-crypto-formatted
                                          currency-symbol
                                          fee-in-fiat))
            show-select-asset-sheet    #(rf/dispatch
                                         [:show-bottom-sheet
                                          {:content (fn []
                                                      [select-asset-bottom-sheet
                                                       clear-input!])}])
            selected-networks          (rf/sub [:wallet/wallet-send-selected-networks])
            affordable-networks        (send-utils/find-affordable-networks
                                        {:balances-per-chain token-balances-per-chain
                                         :input-value        @input-value
                                         :selected-networks  selected-networks
                                         :disabled-chain-ids disabled-from-chain-ids})]
        (rn/use-mount
         (fn []
           (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
                 app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
             #(.remove app-keyboard-listener))))
        (rn/use-effect
         #(when (> (count affordable-networks) 0)
            (fetch-routes input-num-value (current-limit) 2000))
         [@input-value])
        (rn/use-effect
         #(when (> (count affordable-networks) 0)
            (fetch-routes input-num-value (current-limit) 0))
         [disabled-from-chain-ids])
        [rn/view
         {:style               style/screen
          :accessibility-label (str "container" (when @input-error "-error"))}
         [account-switcher/view
          {:icon-name     :i/arrow-left
           :on-press      on-navigate-back
           :switcher-type :select-account}]
         [quo/token-input
          {:container-style     style/input-container
           :token               token-symbol
           :currency            current-currency
           :crypto-decimals     crypto-decimals
           :error?              @input-error
           :networks            (seq token-networks)
           :title               (i18n/label :t/send-limit {:limit limit-label})
           :conversion          conversion-rate
           :show-keyboard?      false
           :value               @input-value
           :selection           @input-selection
           :on-change-text      #(handle-on-change % (current-limit))
           :on-selection-change selection-change
           :on-swap             #(handle-swap
                                  {:crypto?      %
                                   :currency     current-currency
                                   :token-symbol token-symbol
                                   :limit-fiat   fiat-limit
                                   :limit-crypto crypto-limit})
           :on-token-press      show-select-asset-sheet}]
         [routes/view
          {:from-values-by-chain   from-values-by-chain
           :to-values-by-chain     to-values-by-chain
           :affordable-networks    affordable-networks
           :routes                 best-routes
           :token                  token
           :input-value            @input-value
           :fetch-routes           #(fetch-routes % (current-limit) 2000)
           :disabled-from-networks disabled-from-chain-ids
           :on-press-from-network  (fn [chain-id _]
                                     (let [disabled-chain-ids (if (contains? (set
                                                                              disabled-from-chain-ids)
                                                                             chain-id)
                                                                (vec (remove #(= % chain-id)
                                                                             disabled-from-chain-ids))
                                                                (conj disabled-from-chain-ids
                                                                      chain-id))
                                           re-enabling-chain? (< (count disabled-chain-ids)
                                                                 (count disabled-from-chain-ids))]
                                       (when (or re-enabling-chain?
                                                 (> (count affordable-networks) 1))
                                         (rf/dispatch [:wallet/disable-from-networks
                                                       disabled-chain-ids]))))}]
         (when (or loading-routes? (seq route))
           [estimated-fees
            {:loading-suggested-routes? loading-routes?
             :fees                      fee-formatted
             :amount                    amount-text
             :receiver                  (address/get-shortened-key to-address)}])
         [quo/bottom-actions
          {:actions          :one-action
           :button-one-label button-one-label
           :button-one-props (merge button-one-props
                                    {:disabled? confirm-disabled?
                                     :on-press  on-confirm})}]
         [quo/numbered-keyboard
          {:container-style      (style/keyboard-container bottom)
           :left-action          :dot
           :delete-key?          true
           :on-press             #(handle-keyboard-press % loading-routes? (current-limit))
           :on-delete            #(handle-delete loading-routes? (current-limit))
           :on-long-press-delete #(on-long-press-delete loading-routes?)}]]))))

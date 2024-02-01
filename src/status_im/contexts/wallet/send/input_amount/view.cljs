(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
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

(defn- normalize-input
  [current v]
  (cond
    (and (string/blank? current) (= v dot))
    (str "0" v)

    (and (= current "0") (not= v dot))
    (str v)

    :else
    (str current v)))

(defn- make-new-input
  [current v]
  (if (valid-input? current v)
    (normalize-input current v)
    current))

(defn- reset-input-error
  [new-value prev-value input-error]
  (reset! input-error
    (> new-value prev-value)))

(defn- f-view-internal
  ;; crypto-decimals, limit-crypto and initial-crypto-currency? args are needed
  ;; for component tests only
  [{default-on-confirm       :on-confirm
    default-limit-crypto     :limit-crypto
    default-crypto-decimals  :crypto-decimals
    initial-crypto-currency? :initial-crypto-currency?
    :or                      {initial-crypto-currency? true}}]
  (let [_ (rn/dismiss-keyboard!)
        bottom                (safe-area/get-bottom)
        input-value           (reagent/atom "")
        input-error           (reagent/atom false)
        crypto-currency?      (reagent/atom initial-crypto-currency?)
        handle-swap           (fn [{:keys [crypto? limit-fiat limit-crypto]}]
                                (let [num-value     (parse-double @input-value)
                                      current-limit (if crypto? limit-crypto limit-fiat)]
                                  (reset! crypto-currency? crypto?)
                                  (reset-input-error num-value current-limit input-error)))
        handle-keyboard-press (fn [v loading-routes? current-limit-amount]
                                (let [current-value @input-value
                                      new-value     (make-new-input current-value v)
                                      num-value     (or (parse-double new-value) 0)]
                                  (when (not loading-routes?)
                                    (reset! input-value new-value)
                                    (reset-input-error num-value current-limit-amount input-error)
                                    (reagent/flush))))
        handle-delete         (fn [loading-routes? current-limit-amount]
                                (when-not loading-routes?
                                  (swap! input-value #(subs % 0 (dec (count %))))
                                  (reset-input-error @input-value current-limit-amount input-error)
                                  (reagent/flush)))
        handle-on-change      (fn [v current-limit-amount]
                                (when (valid-input? @input-value v)
                                  (let [num-value (or (parse-double v) 0)]
                                    (reset! input-value v)
                                    (reset-input-error num-value current-limit-amount input-error)
                                    (reagent/flush))))
        on-navigate-back      (fn []
                                (rf/dispatch [:wallet/clean-selected-token])
                                (rf/dispatch [:navigate-back-within-stack :wallet-send-input-amount]))
        fetch-routes          (fn [input-num-value current-limit-amount]
                                (rf/dispatch [:wallet/clean-suggested-routes])
                                (when-not (or (empty? @input-value)
                                              (<= input-num-value 0)
                                              (> input-num-value current-limit-amount))
                                  (debounce/debounce-and-dispatch
                                   [:wallet/get-suggested-routes @input-value]
                                   100)))
        handle-on-confirm     (fn []
                                (rf/dispatch [:wallet/send-select-amount
                                              {:amount   @input-value
                                               :stack-id :wallet-send-input-amount}]))]
    (fn []
      (let [{fiat-currency :currency} (rf/sub [:profile/profile])
            {:keys [color]}           (rf/sub [:wallet/current-viewing-account])
            {token-balance  :total-balance
             token-symbol   :symbol
             token-networks :networks
             :as            token}    (rf/sub [:wallet/wallet-send-token])
            conversion-rate           (-> token :market-values-per-currency :usd :price)
            loading-routes?           (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
            suggested-routes          (rf/sub [:wallet/wallet-send-suggested-routes])
            route                     (rf/sub [:wallet/wallet-send-route])
            on-confirm                (or default-on-confirm handle-on-confirm)
            crypto-decimals           (or default-crypto-decimals
                                          (utils/get-crypto-decimals-count token))
            crypto-limit              (or default-limit-crypto
                                          (utils/get-standard-crypto-format token token-balance))
            fiat-limit                (.toFixed (* token-balance conversion-rate) 2)
            current-limit             (if @crypto-currency? crypto-limit fiat-limit)
            current-currency          (if @crypto-currency? token-symbol fiat-currency)
            limit-label               (make-limit-label {:amount   current-limit
                                                         :currency current-currency})
            input-num-value           (parse-double @input-value)
            confirm-disabled?         (or (nil? route)
                                          (empty? @input-value)
                                          (<= input-num-value 0)
                                          (> input-num-value current-limit))
            amount-text               (str @input-value " " token-symbol)]
        (rn/use-effect
         (fn []
           (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
                 app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
             #(.remove app-keyboard-listener))))
        (rn/use-effect
         #(fetch-routes input-num-value current-limit)
         [@input-value])
        [rn/view
         {:style               style/screen
          :accessibility-label (str "container" (when @input-error "-error"))}
         [account-switcher/view
          {:icon-name     :i/arrow-left
           :on-press      on-navigate-back
           :switcher-type :select-account}]
         [quo/token-input
          {:container-style style/input-container
           :token           token-symbol
           :currency        current-currency
           :crypto-decimals crypto-decimals
           :error?          @input-error
           :networks        token-networks
           :title           (i18n/label :t/send-limit {:limit limit-label})
           :conversion      conversion-rate
           :show-keyboard?  false
           :value           @input-value
           :on-change-text  #(handle-on-change % current-limit)
           :on-swap         #(handle-swap
                              {:crypto?      %
                               :currency     current-currency
                               :token-symbol token-symbol
                               :limit-fiat   fiat-limit
                               :limit-crypto crypto-limit})}]
         [routes/view
          {:amount       amount-text
           :routes       suggested-routes
           :token        token
           :input-value  @input-value
           :fetch-routes #(fetch-routes % current-limit)}]
         [quo/bottom-actions
          {:actions             :1-action
           :customization-color color
           :button-one-label    (i18n/label :t/confirm)
           :button-one-props    {:disabled? confirm-disabled?
                                 :on-press  on-confirm}}]
         [quo/numbered-keyboard
          {:container-style (style/keyboard-container bottom)
           :left-action     :dot
           :delete-key?     true
           :on-press        #(handle-keyboard-press % loading-routes? current-limit)
           :on-delete       #(handle-delete loading-routes? current-limit)}]]))))

(defn- view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))

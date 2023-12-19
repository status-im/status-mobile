(ns status-im2.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im2.contexts.wallet.send.input-amount.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- make-limit-label
  [{:keys [amount currency]}]
  (str amount " " (string/upper-case (name currency))))

(def not-digits-or-dot-pattern
  #"[^0-9+\.]")

(def dot ".")

(defn valid-input?
  [current v]
  (let [max-length          12
        length-owerflow?    (>= (count current) max-length)
        extra-dot?          (and (= v dot) (string/includes? current dot))
        extra-leading-zero? (and (= current "0") (= "0" (str v)))
        non-numeric?        (re-find not-digits-or-dot-pattern (str v))]
    (not (or non-numeric? extra-dot? extra-leading-zero? length-owerflow?))))

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

(defn- f-view-internal
  [{:keys [rate limit]}]
  (let [bottom                    (safe-area/get-bottom)
        {:keys [currency]}        (rf/sub [:profile/profile])
        networks                  (rf/sub [:wallet/network-details])
        token                     (rf/sub [:wallet/wallet-send-token])
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        token-symbol              (:symbol token)
        limit-crypto              (or (:total-balance token) limit)
        conversion-rate           (or rate 10)
        limit-fiat                (* limit-crypto conversion-rate)
        input-value               (reagent/atom "")
        current-limit             (reagent/atom {:amount   limit-crypto
                                                 :currency token-symbol})
        handle-swap               (fn [crypto?]
                                    (let [num-value (parse-double @input-value)]
                                      (reset! current-limit (if crypto?
                                                              {:amount   limit-crypto
                                                               :currency token-symbol}
                                                              {:amount   limit-fiat
                                                               :currency currency}))
                                      (when (> num-value (:amount @current-limit))
                                        (reset! input-value ""))))
        handle-keyboard-press     (fn [v]
                                    (let [current-value @input-value
                                          new-value     (make-new-input current-value v)
                                          num-value     (or (parse-double new-value) 0)]
                                      (when (and (not loading-suggested-routes?)
                                                 (<= num-value (:amount @current-limit)))
                                        (reset! input-value new-value)
                                        (reagent/flush))))
        handle-delete             (fn [_]
                                    (when-not loading-suggested-routes?
                                      (swap! input-value #(subs % 0 (dec (count %))))
                                      (reagent/flush)))
        handle-on-change          (fn [v]
                                    (when (valid-input? @input-value v)
                                      (let [num-value            (or (parse-double v) 0)
                                            current-limit-amount (:amount @current-limit)]
                                        (if (> num-value current-limit-amount)
                                          (reset! input-value (str current-limit-amount))
                                          (reset! input-value v))
                                        (reagent/flush))))]
    (fn [{:keys [on-confirm]
          :or   {on-confirm #(rf/dispatch [:wallet/send-select-amount
                                           {:amount   @input-value
                                            :stack-id :wallet-send-input-amount}])}}]
      (let [limit-label               (make-limit-label @current-limit)
            input-num-value           (parse-double @input-value)
            route                     (rf/sub [:wallet/wallet-send-route])
            loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
            confirm-disabled?         (or
                                       (nil? route)
                                       (empty? @input-value)
                                       (<= input-num-value 0)
                                       (> input-num-value (:amount @current-limit)))]
        (rn/use-effect
         (fn []
           (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
                 app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
             #(.remove app-keyboard-listener))))
        (rn/use-effect (fn []
                         (rf/dispatch [:wallet/clean-suggested-routes])
                         (when-not (or
                                    (empty? @input-value)
                                    (<= input-num-value 0)
                                    (> input-num-value (:amount @current-limit)))
                           (debounce/debounce-and-dispatch [:wallet/get-suggested-routes @input-value]
                                                           100)))
                       [@input-value])
        [rn/view
         {:style style/screen}
         [account-switcher/view
          {:icon-name     :i/arrow-left
           :on-press      #(rf/dispatch [:navigate-back-within-stack :wallet-send-input-amount])
           :switcher-type :select-account}]
         [quo/token-input
          {:container-style style/input-container
           :token           token-symbol
           :currency        currency
           :networks        networks
           :title           (i18n/label :t/send-limit {:limit limit-label})
           :conversion      conversion-rate
           :show-keyboard?  false
           :value           @input-value
           :on-swap         handle-swap
           :on-change-text  (fn [text]
                              (handle-on-change text))}]
         ;; Network routing content to be added
         [rn/scroll-view
          {:content-container-style {:flex-grow       1
                                     :align-items     :center
                                     :justify-content :center}}
          (cond loading-suggested-routes?
                [quo/text "Loading routes"]
                (and (not loading-suggested-routes?) route)
                [quo/text "Route found"]
                (and (not loading-suggested-routes?) (nil? route))
                [quo/text "Route not found"])]
         [quo/bottom-actions
          {:actions          :1-action
           :button-one-label (i18n/label :t/confirm)
           :button-one-props {:disabled? confirm-disabled?
                              :on-press  on-confirm}}]
         [quo/numbered-keyboard
          {:container-style (style/keyboard-container bottom)
           :left-action     :dot
           :delete-key?     true
           :on-press        handle-keyboard-press
           :on-delete       handle-delete}]]))))

(defn- view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))

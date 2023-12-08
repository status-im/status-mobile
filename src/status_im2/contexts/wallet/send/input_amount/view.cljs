(ns status-im2.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.send.input-amount.style :as style]
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
  [{:keys [token limit rate]}]
  (let [bottom                (safe-area/get-bottom)
        {:keys [currency]}    (rf/sub [:profile/profile])
        networks              (rf/sub [:wallet/network-details])
        ;; Temporary values
        token                 (or token :eth)
        conversion-rate       (or rate 10)
        limit-crypto          (or limit 2860000.32)
        limit-fiat            (* limit-crypto conversion-rate)
        input-value           (reagent/atom "")
        current-limit         (reagent/atom {:amount   limit-crypto
                                             :currency token})
        handle-swap           (fn [crypto?]
                                (let [num-value (parse-double @input-value)]
                                  (reset! current-limit (if crypto?
                                                          {:amount   limit-crypto
                                                           :currency token}
                                                          {:amount   limit-fiat
                                                           :currency currency}))
                                  (when (> num-value (:amount @current-limit))
                                    (reset! input-value ""))))
        handle-keyboard-press (fn [v]
                                (let [current-value @input-value
                                      new-value     (make-new-input current-value v)
                                      num-value     (or (parse-double new-value) 0)]
                                  (when (<= num-value (:amount @current-limit))
                                    (reset! input-value new-value)
                                    (reagent/flush))))
        handle-delete         (fn [_]
                                (swap! input-value #(subs % 0 (dec (count %))))
                                (reagent/flush))
        handle-on-change      (fn [v]
                                (when (valid-input? @input-value v)
                                  (let [num-value            (or (parse-double v) 0)
                                        current-limit-amount (:amount @current-limit)]
                                    (if (> num-value current-limit-amount)
                                      (reset! input-value (str current-limit-amount))
                                      (reset! input-value v))
                                    (reagent/flush))))]
    (fn [{:keys [on-confirm]
          :or   {on-confirm #(js/alert "Confirmed")}}]
      (let [limit-label       (make-limit-label @current-limit)
            input-num-value   (parse-double @input-value)
            confirm-disabled? (or
                               (empty? @input-value)
                               (<= input-num-value 0)
                               (> input-num-value (:amount @current-limit)))]
        (rn/use-effect
         (fn []
           (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
                 app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
             #(.remove app-keyboard-listener))))
        [rn/view
         {:style style/screen}
         [quo/page-nav
          {:background       :blur
           :icon-name        :i/arrow-left
           :on-press         #(rf/dispatch [:navigate-back])
           :right-side       :account-switcher
           :account-switcher {:customization-color :yellow
                              :emoji               "🎮"
                              :on-press            #(js/alert "Switch account")}}]
         [quo/token-input
          {:container-style style/input-container
           :token           token
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
         [rn/scroll-view]
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

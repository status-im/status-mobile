(ns status-im2.contexts.wallet.send.input-amount.view
  (:require
   [clojure.string :as string]
   [quo.core :as quo]
   [quo.theme :as quo.theme]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [status-im2.contexts.wallet.send.input-amount.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- make-limit-label
  [{:keys [amount currency]}]
  (str amount " " (string/upper-case (name currency))))

(defn- make-new-input
  [current v]
  (let [dot               "."
        max-length        12
        length-owerflow?  (>= (count current) max-length)
        ignore-extra-dot? (and (= v dot) (string/includes? current dot))
        ignore-value?     (or
                           ignore-extra-dot?
                           length-owerflow?)]
    (if ignore-value?
      current
      (str current v))))

(defn- view-internal
  [_]
  (let [token                 :eth
        {:keys [currency]}    (rf/sub [:profile/profile])
        networks              (rf/sub [:wallet/network-details])
        conversion-rate       10
        limit-crypto          286.32
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
                                      num-value     (parse-double new-value)]
                                  (when (<= num-value (:amount @current-limit))
                                    (reset! input-value new-value))))
        handle-delete         (fn [_]
                                (swap! input-value #(subs % 0 (dec (count %)))))]
    (fn [{:keys [on-confirm]
          :or   {on-confirm #(js/alert "Confirmed")}}]
      (let [limit-label (make-limit-label @current-limit)]
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
           :on-swap         handle-swap}]
         ;; Network routing content to be added
         [rn/scroll-view]
         [quo/bottom-actions
          {:actions          :1-action
           :button-one-label (i18n/label :t/confirm)
           :button-one-props {:disabled? (empty? @input-value)
                              :on-press  on-confirm}}]
         [quo/numbered-keyboard
          {:left-action :dot
           :delete-key? true
           :on-press    handle-keyboard-press
           :on-delete   handle-delete}]]))))

(def view (quo.theme/with-theme view-internal))

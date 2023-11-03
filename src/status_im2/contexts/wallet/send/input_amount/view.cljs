(ns status-im2.contexts.wallet.send.input-amount.view
  (:require
   [clojure.string :as string]
   [quo.core :as quo]
   [quo.theme :as quo.theme]
   [react-native.core :as rn]
   [react-native.safe-area :as safe-area]
   [reagent.core :as reagent]
   [status-im2.contexts.wallet.common.temp :as temp]
   [status-im2.contexts.wallet.send.input-amount.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- view-internal
  [_]
  (let [top                   (safe-area/get-top)
        token                 :eth
        currency              :usd
        networks              temp/networks-list
        limit                 286.32
        limit-label           (str limit " " (string/upper-case (name token)))
        input-title           (i18n/label :t/send-limit {:limit limit-label})
        input-value           (reagent/atom "")
        conversion-rate       1800
        handle-keyboard-press (fn [v]
                                (let [new-value (str @input-value v)
                                      num-value (parse-double new-value)]
                                  (prn "=========")
                                  (prn v)

                                  (prn "=========")

                                  (when (<= num-value limit)
                                    (reset! input-value new-value))))
        handle-delete         (fn [_]
                                (swap! input-value #(subs % 0 (dec (count %)))))]
    (fn [{:keys [theme on-confirm]
          :or   {on-confirm #(js/alert "Confirmed")}}]
      [rn/view
       {:style (style/screen top)}
       [quo/page-nav
        {:background       :blur
         :icon-name        :i/arrow-left
         :on-press         #(rf/dispatch [:navigate-back])
         :right-side       :account-switcher
         :account-switcher {:customization-color :yellow
                            :emoji               "🎮"
                            :on-press            #(js/alert "Switch account")}}]
       [quo/token-input
        {:token          token
         :currency       currency
         :networks       networks
         :title          input-title
         :conversion     conversion-rate
         :show-keyboard? false
         :value          @input-value}]


       [rn/scroll-view]
       [quo/bottom-actions
        {:actions          :1-action
         :button-one-label (i18n/label :t/confirm)
         :button-one-props {:disabled? (empty? @input-value)
                            :on-press  on-confirm}}]
       [quo/numbered-keyboard
        {:left-action :dot
         :delete-key? true
         :theme       theme
         :on-press    handle-keyboard-press
         :on-delete   handle-delete}]])))

(def view (quo.theme/with-theme view-internal))

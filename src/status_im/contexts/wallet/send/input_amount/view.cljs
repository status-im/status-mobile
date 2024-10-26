(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.events]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.contexts.wallet.sheets.unpreferred-networks-alert.view :as unpreferred-networks-alert]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
    [status-im.subs.wallet.screens.input-amount]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]))

(defn- estimated-fees
  [{:keys [loading-routes? fees amount]}]
  [rn/view {:style style/estimated-fees-container}
   (when (ff/enabled? ::ff/wallet.advanced-sending)
     [rn/view {:style style/estimated-fees-content-container}
      [quo/button
       {:icon-only?          true
        :type                :outline
        :size                32
        :inner-style         {:opacity 1}
        :accessibility-label :advanced-button
        :disabled?           loading-routes?
        :on-press            #(js/alert "Not implemented yet")}
       :i/advanced]])
   [quo/data-item
    {:container-style style/fees-data-item
     :status          (if loading-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/fees)
     :subtitle        fees}]
   [quo/data-item
    {:container-style style/amount-data-item
     :status          (if loading-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/recipient-gets)
     :subtitle        amount}]])

(defn- every-network-value-is-zero?
  [sender-network-values]
  (every? (fn [{:keys [total-amount]}]
            (and
             total-amount
             (money/bignumber? total-amount)
             (money/equal-to total-amount
                             (money/bignumber "0"))))
          sender-network-values))

(defn select-asset-bottom-sheet
  []
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
                                   (rf/dispatch
                                    [:send-input-amount-screen/token-input-delete-all]))}]]))

(defn- token-not-available
  [token-symbol receiver-networks token-networks]
  (let [theme              (quo.theme/use-theme)
        add-token-networks (fn []
                             (let [chain-ids (concat receiver-networks
                                                     (mapv #(:chain-id %) token-networks))]
                               (rf/dispatch [:wallet/update-receiver-networks chain-ids])))]
    [rn/view {:style (style/token-not-available-container theme)}
     [rn/view
      [quo/icon :i/alert
       {:size  16
        :color (colors/resolve-color :danger theme)}]]
     [rn/view {:style style/token-not-available-content-container}
      [quo/text
       {:style (style/token-not-available-text theme)
        :size  :paragraph-2}
       (i18n/label :t/token-not-available-on-receiver-networks {:token-symbol token-symbol})]
      [quo/button
       {:size                24
        :customization-color (colors/resolve-color :danger theme)
        :on-press            add-token-networks}
       (i18n/label :t/add-networks-token-can-be-sent-to {:token-symbol token-symbol})]]]))

(defn- show-unpreferred-networks-alert
  [on-confirm]
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn []
                [unpreferred-networks-alert/view
                 {:on-confirm on-confirm}])}]))

(defn- no-routes-found
  []
  [rn/view {:style style/no-routes-found-container}
   [quo/info-message
    {:status          :error
     :icon            :i/alert
     :size            :default
     :container-style {:margin-top 15}}
    (i18n/label :t/no-routes-found)]])

(defn- not-enough-asset
  []
  [quo/alert-banner
   {:action?         true
    :text            (i18n/label :t/not-enough-assets-to-pay-gas-fees)
    :button-text     (i18n/label :t/add-eth)
    :on-button-press #(rf/dispatch [:show-bottom-sheet
                                    {:content buy-token/view}])}])

(defn- fetch-routes
  [{:keys [amount bounce-duration-ms token valid-input? reset-amounts-to-zero?]}]
  (cond
    valid-input?
    (debounce/debounce-and-dispatch
     [:wallet/start-get-suggested-routes
      {:amount        amount
       :updated-token token}]
     bounce-duration-ms)

    reset-amounts-to-zero?
    (rf/dispatch [:wallet/reset-network-amounts-to-zero])

    :else (rf/dispatch [:wallet/stop-and-clean-suggested-routes])))

(defn- get-fee-formatted
  [route]
  (when-let [native-currency-symbol (-> route first :from :native-currency-symbol)]
    (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol])))

(defn view
  ;; crypto-decimals, limit-crypto and initial-crypto-currency? args are needed
  ;; for component tests only
  [{on-confirm        :on-confirm
    on-navigate-back  :on-navigate-back
    button-one-label  :button-one-label
    button-one-props  :button-one-props
    current-screen-id :current-screen-id}]
  (let [view-id (rf/sub [:view-id])
        active-screen? (= view-id current-screen-id)
        bottom (safe-area/get-bottom)
        show-select-asset-sheet #(rf/dispatch
                                  [:show-bottom-sheet
                                   {:content (fn [] [select-asset-bottom-sheet])}])
        {:keys [input-value crypto-currency?]} (rf/sub [:send-input-amount-screen/state])
        from-enabled-networks (rf/sub [:send-input-amount-screen/from-enabled-networks])
        {fiat-currency :currency} (rf/sub [:profile/profile])
        {token-symbol   :symbol
         token-networks :networks
         :as            token} (rf/sub [:wallet/wallet-send-token])
        send-from-locked-amounts (rf/sub [:wallet/wallet-send-from-locked-amounts])
        token-by-symbol (rf/sub [:send-input-amount-screen/token-by-symbol])
        conversion-rate (rf/sub [:send-input-amount-screen/conversion-rate])
        token-input-converted-value (rf/sub [:send-input-amount-screen/token-input-converted-value])
        max-decimals (rf/sub [:send-input-amount-screen/max-decimals])
        currency-symbol (rf/sub [:profile/currency-symbol])
        loading-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        route (rf/sub [:wallet/wallet-send-route])
        valid-input? (rf/sub [:send-input-amount-screen/valid-input?])
        amount-in-crypto (rf/sub [:send-input-amount-screen/amount-in-crypto])
        recipient-gets-amount (rf/sub [:send-input-amount-screen/recipient-gets-amount])
        sender-network-values (rf/sub [:wallet/wallet-send-sender-network-values])
        unsupported-token-in-receiver? (rf/sub
                                        [:send-input-amount-screen/unsupported-token-in-receiver?])
        routes (rf/sub [:send-input-amount-screen/routes])
        no-routes-found? (rf/sub [:send-input-amount-screen/no-routes-found?])
        receiver-networks (rf/sub [:wallet/wallet-send-receiver-networks])
        sending-to-unpreferred-networks? (rf/sub
                                          [:send-input-amount-screen/sending-to-unpreferred-networks?])
        value-out-of-limits? (rf/sub [:send-input-amount-screen/value-out-of-limits?])
        limit-exceeded? (rf/sub [:send-input-amount-screen/upper-limit-exceeded?])
        upper-limit-prettified (rf/sub [:send-input-amount-screen/upper-limit-prettified])
        current-address (rf/sub [:wallet/current-viewing-account-address])
        current-color (rf/sub [:wallet/current-viewing-account-color])
        not-enough-asset? (rf/sub [:send-input-amount-screen/not-enough-asset?])
        should-try-again? (rf/sub [:send-input-amount-screen/should-try-again?])
        show-no-routes? (rf/sub [:send-input-amount-screen/show-no-routes?])
        confirm-disabled? (rf/sub [:send-input-amount-screen/confirm-disabled?])
        fee-formatted (rf/sub [:send-input-amount-screen/fee-formatted])
        request-fetch-routes (fn [bounce-duration-ms]
                               (fetch-routes
                                {:amount                 amount-in-crypto
                                 :valid-input?           valid-input?
                                 :bounce-duration-ms     bounce-duration-ms
                                 :token                  token
                                 :reset-amounts-to-zero? (and limit-exceeded?
                                                              (some? routes))}))]
    (rn/use-effect
     (fn []
       (when active-screen?
         (rn/dismiss-keyboard!)))
     [active-screen?])
    (rn/use-mount
     (fn []
       (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
             app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
         #(.remove app-keyboard-listener))))
    (hot-reload/use-safe-unmount on-navigate-back)
    (rn/use-effect
     (fn []
       (when value-out-of-limits?
         (rf/dispatch [:wallet/stop-get-suggested-routes])
         (debounce/clear-all)))
     [value-out-of-limits?])
    (rn/use-effect
     (fn []
       (rf/dispatch [:send-input-amount-screen/token-input-delete-all])
       (rf/dispatch [:wallet/stop-and-clean-suggested-routes])
       (rf/dispatch [:wallet/clean-disabled-from-networks]))
     [current-address])
    (rn/use-effect
     (fn []
       (request-fetch-routes 0))
     [send-from-locked-amounts])
    [rn/view
     {:style               style/screen
      :accessibility-label (str "container"
                                (when value-out-of-limits? "-error"))}
     [account-switcher/view
      {:icon-name     :i/arrow-left
       :on-press      #(rf/dispatch [:navigate-back])
       :switcher-type :select-account}]
     [quo/token-input
      {:container-style style/input-container
       :token-symbol    token-symbol
       :value           input-value
       :on-swap         #(rf/dispatch [:send-input-amount-screen/swap-between-fiat-and-crypto
                                       token-input-converted-value])
       :on-token-press  show-select-asset-sheet
       :error?          value-out-of-limits?
       :currency-symbol (if crypto-currency? token-symbol fiat-currency)
       :converted-value (if crypto-currency?
                          (utils/prettify-balance
                           currency-symbol
                           (money/crypto->fiat input-value
                                               conversion-rate))
                          (utils/prettify-crypto-balance
                           (or (clj->js token-symbol) "")
                           (money/fiat->crypto input-value
                                               conversion-rate)
                           conversion-rate))
       :hint-component  [quo/network-tags
                         {:networks (seq from-enabled-networks)
                          :title    (i18n/label
                                     :t/send-limit
                                     {:limit upper-limit-prettified})
                          :status   (when value-out-of-limits? :error)}]}]
     [routes/view
      {:token                                     token-by-symbol
       :send-amount-in-crypto                     amount-in-crypto
       :valid-input?                              valid-input?
       :token-not-supported-in-receiver-networks? unsupported-token-in-receiver?
       :current-screen-id                         current-screen-id
       :request-fetch-routes                      request-fetch-routes}]
     (when (and (not loading-routes?)
                sender-network-values
                unsupported-token-in-receiver?)
       [token-not-available token-symbol receiver-networks token-networks])
     (when not-enough-asset?
       [not-enough-asset])
     (when (or (and (not no-routes-found?) (or loading-routes? route))
               not-enough-asset?)
       [estimated-fees
        {:loading-routes? loading-routes?
         :fees            fee-formatted
         :amount          recipient-gets-amount}])
     (when show-no-routes?
       [no-routes-found])
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (if should-try-again?
                           (i18n/label :t/try-again)
                           button-one-label)
       :button-one-props (merge (when-not should-try-again?
                                  button-one-props)
                                {:disabled?           (or not-enough-asset?
                                                          loading-routes?
                                                          (and (not should-try-again?)
                                                               confirm-disabled?))
                                 :on-press            (cond
                                                        should-try-again?
                                                        #(rf/dispatch [:wallet/start-get-suggested-routes
                                                                       {:amount        amount-in-crypto
                                                                        :updated-token token-by-symbol}])
                                                        sending-to-unpreferred-networks?
                                                        #(show-unpreferred-networks-alert on-confirm)
                                                        :else
                                                        #(on-confirm amount-in-crypto))
                                 :customization-color current-color}
                                (when should-try-again?
                                  {:type :grey}))}]
     [quo/numbered-keyboard
      {:container-style      (style/keyboard-container bottom)
       :left-action          :dot
       :delete-key?          true
       :on-press             (fn [c]
                               (rf/dispatch [:send-input-amount-screen/token-input-add-character c
                                             max-decimals]))
       :on-delete            (fn []
                               (debounce/clear-all)
                               (rf/dispatch [:send-input-amount-screen/token-input-delete-last])
                               (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes]))
       :on-long-press-delete (fn []
                               (debounce/clear-all)
                               (rf/dispatch [:send-input-amount-screen/token-input-delete-all])
                               (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes]))}]]))


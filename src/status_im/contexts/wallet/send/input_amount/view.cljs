(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.contexts.wallet.sheets.unpreferred-networks-alert.view :as unpreferred-networks-alert]
    [status-im.feature-flags :as ff]
    [status-im.setup.hot-reload :as hot-reload]
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
    :button-text     (i18n/label :t/buy-eth)
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
    enabled-from-chain-ids   :enabled-from-chain-ids
    from-enabled-networks    :from-enabled-networks
    :or                      {initial-crypto-currency? true}}]
  (let [view-id                                   (rf/sub [:view-id])
        active-screen?                            (= view-id current-screen-id)
        bottom                                    (safe-area/get-bottom)
        [crypto-currency? set-crypto-currency]    (rn/use-state initial-crypto-currency?)
        handle-on-confirm                         (fn [amount]
                                                    (rf/dispatch [:wallet/set-token-amount-to-send
                                                                  {:amount   amount
                                                                   :stack-id current-screen-id}]))
        {fiat-currency :currency}                 (rf/sub [:profile/profile])
        {token-symbol   :symbol
         token-networks :networks
         :as            token}                    (rf/sub [:wallet/wallet-send-token])
        send-from-locked-amounts                  (rf/sub [:wallet/wallet-send-from-locked-amounts])
        {:keys [total-balance]
         :as   token-by-symbol}                   (rf/sub [:wallet/token-by-symbol
                                                           (str token-symbol)
                                                           enabled-from-chain-ids])
        token-balance                             (or default-limit-crypto total-balance)
        usd-conversion-rate                       (utils/token-usd-price token)
        currency                                  (rf/sub [:profile/currency])
        conversion-rate                           (-> token
                                                      :market-values-per-currency
                                                      currency
                                                      :price)
        token-decimals                            (-> token
                                                      utils/token-usd-price
                                                      utils/one-cent-value
                                                      utils/calc-max-crypto-decimals)
        [input-state set-input-state]             (rn/use-state controlled-input/init-state)
        clear-input!                              #(set-input-state controlled-input/delete-all)
        currency-symbol                           (rf/sub [:profile/currency-symbol])
        loading-routes?                           (rf/sub
                                                   [:wallet/wallet-send-loading-suggested-routes?])
        route                                     (rf/sub [:wallet/wallet-send-route])
        on-confirm                                (or default-on-confirm handle-on-confirm)
        crypto-decimals                           (or token-decimals default-crypto-decimals)
        max-limit                                 (if crypto-currency?
                                                    (utils/cut-crypto-decimals-to-fit-usd-cents
                                                     token-balance
                                                     usd-conversion-rate)
                                                    (utils/cut-fiat-balance-to-two-decimals
                                                     (money/crypto->fiat token-balance conversion-rate)))
        input-value                               (controlled-input/input-value input-state)
        valid-input?                              (not (or (controlled-input/empty-value? input-state)
                                                           (controlled-input/input-error input-state)))
        confirm-disabled?                         (or (nil? route)
                                                      (empty? route)
                                                      (not valid-input?))
        amount-in-crypto                          (if crypto-currency?
                                                    input-value
                                                    (number/remove-trailing-zeroes
                                                     (.toFixed (/ input-value conversion-rate)
                                                               crypto-decimals)))
        total-amount-receiver                     (rf/sub [:wallet/total-amount true])
        amount-text                               (str (number/remove-trailing-zeroes
                                                        (.toFixed total-amount-receiver
                                                                  (min token-decimals 6)))
                                                       " "
                                                       token-symbol)
        first-route                               (first route)
        native-currency-symbol                    (when-not confirm-disabled?
                                                    (get-in first-route
                                                            [:from :native-currency-symbol]))
        fee-formatted                             (when native-currency-symbol
                                                    (rf/sub [:wallet/wallet-send-fee-fiat-formatted
                                                             native-currency-symbol]))
        show-select-asset-sheet                   #(rf/dispatch
                                                    [:show-bottom-sheet
                                                     {:content (fn []
                                                                 [select-asset-bottom-sheet
                                                                  clear-input!])}])
        sender-network-values                     (rf/sub
                                                   [:wallet/wallet-send-sender-network-values])
        receiver-network-values                   (rf/sub
                                                   [:wallet/wallet-send-receiver-network-values])
        tx-type                                   (rf/sub [:wallet/wallet-send-tx-type])
        token-not-supported-in-receiver-networks? (and (not= tx-type :tx/bridge)
                                                       (->> receiver-network-values
                                                            (remove #(= (:type %) :add))
                                                            (every? #(= (:type %) :not-available))))
        suggested-routes                          (rf/sub [:wallet/wallet-send-suggested-routes])
        routes                                    (when suggested-routes
                                                    (or (:best suggested-routes) []))
        no-routes-found?                          (and
                                                   (every-network-value-is-zero?
                                                    sender-network-values)
                                                   (not (nil? routes))
                                                   (not loading-routes?)
                                                   (not token-not-supported-in-receiver-networks?))
        receiver-networks                         (rf/sub [:wallet/wallet-send-receiver-networks])
        receiver-preferred-networks               (rf/sub
                                                   [:wallet/wallet-send-receiver-preferred-networks])
        receiver-preferred-networks-set           (set receiver-preferred-networks)
        sending-to-unpreferred-networks?          (not (every? (fn [receiver-selected-network]
                                                                 (contains?
                                                                  receiver-preferred-networks-set
                                                                  receiver-selected-network))
                                                               receiver-networks))
        input-error                               (controlled-input/input-error input-state)
        limit-exceeded?                           (controlled-input/upper-limit-exceeded? input-state)
        should-try-again?                         (and (not limit-exceeded?) no-routes-found?)
        current-address                           (rf/sub [:wallet/current-viewing-account-address])
        owned-eth-token                           (rf/sub [:wallet/token-by-symbol
                                                           (string/upper-case
                                                            constants/mainnet-short-name)
                                                           enabled-from-chain-ids])
        not-enough-asset?                         (and
                                                   (or no-routes-found? limit-exceeded?)
                                                   (not-empty sender-network-values)
                                                   (if (= token-symbol
                                                          (string/upper-case
                                                           constants/mainnet-short-name))
                                                     (money/equal-to
                                                      (controlled-input/value-bn input-state)
                                                      (controlled-input/upper-limit-bn input-state))
                                                     (money/equal-to (:total-balance
                                                                      owned-eth-token)
                                                                     0)))
        show-no-routes?                           (and
                                                   (or no-routes-found? limit-exceeded?)
                                                   (not-empty sender-network-values)
                                                   (not not-enough-asset?))
        request-fetch-routes                      (fn [bounce-duration-ms]
                                                    (fetch-routes
                                                     {:amount                 amount-in-crypto
                                                      :valid-input?           valid-input?
                                                      :bounce-duration-ms     bounce-duration-ms
                                                      :token                  token
                                                      :reset-amounts-to-zero? (and limit-exceeded?
                                                                                   (some? routes))}))
        swap-between-fiat-and-crypto              (fn []
                                                    (if crypto-currency?
                                                      (set-input-state
                                                       #(controlled-input/->fiat % conversion-rate))
                                                      (set-input-state
                                                       #(controlled-input/->crypto % conversion-rate)))
                                                    (set-crypto-currency (not crypto-currency?)))]
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
       (set-input-state #(controlled-input/set-upper-limit % max-limit)))
     [max-limit])
    (rn/use-effect
     (fn []
       (when input-error
         (rf/dispatch [:wallet/stop-get-suggested-routes])
         (debounce/clear-all)))
     [input-error])
    (rn/use-effect
     (fn []
       (clear-input!)
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
                                (when (controlled-input/input-error input-state) "-error"))}
     [account-switcher/view
      {:icon-name     :i/arrow-left
       :on-press      #(rf/dispatch [:navigate-back])
       :switcher-type :select-account}]
     [quo/token-input
      {:container-style style/input-container
       :token-symbol    token-symbol
       :value           input-value
       :on-swap         swap-between-fiat-and-crypto
       :on-token-press  show-select-asset-sheet
       :error?          (controlled-input/input-error input-state)
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
                                     {:limit (if crypto-currency?
                                               (utils/prettify-crypto-balance
                                                (or (clj->js token-symbol) "")
                                                (controlled-input/upper-limit-bn input-state)
                                                conversion-rate)
                                               (utils/prettify-balance currency-symbol
                                                                       (controlled-input/upper-limit-bn
                                                                        input-state)))})
                          :status   (when (controlled-input/input-error input-state) :error)}]}]
     [routes/view
      {:token                                     token-by-symbol
       :send-amount-in-crypto                     amount-in-crypto
       :valid-input?                              valid-input?
       :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
       :current-screen-id                         current-screen-id
       :request-fetch-routes                      request-fetch-routes}]
     (when (and (not loading-routes?)
                sender-network-values
                token-not-supported-in-receiver-networks?)
       [token-not-available token-symbol receiver-networks token-networks])
     (when (and (not no-routes-found?) (or loading-routes? route))
       [estimated-fees
        {:loading-routes? loading-routes?
         :fees            fee-formatted
         :amount          amount-text}])
     (cond
       show-no-routes?   [no-routes-found]
       not-enough-asset? [not-enough-asset])
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (if should-try-again?
                           (i18n/label :t/try-again)
                           button-one-label)
       :button-one-props (merge (when-not should-try-again?
                                  button-one-props)
                                {:disabled? (or loading-routes?
                                                (and (not should-try-again?) confirm-disabled?))
                                 :on-press  (cond
                                              should-try-again?
                                              #(rf/dispatch [:wallet/start-get-suggested-routes
                                                             {:amount        amount-in-crypto
                                                              :updated-token token-by-symbol}])
                                              sending-to-unpreferred-networks?
                                              #(show-unpreferred-networks-alert on-confirm)
                                              :else
                                              #(on-confirm amount-in-crypto))}
                                (when should-try-again?
                                  {:type :grey}))}]
     [quo/numbered-keyboard
      {:container-style      (style/keyboard-container bottom)
       :left-action          :dot
       :delete-key?          true
       :on-press             (fn [c]
                               (let [new-text      (str input-value c)
                                     max-decimals  (if crypto-currency? crypto-decimals 2)
                                     regex-pattern (str "^\\d*\\.?\\d{0," max-decimals "}$")
                                     regex         (re-pattern regex-pattern)]
                                 (when (re-matches regex new-text)
                                   (debounce/clear-all)
                                   (set-input-state #(controlled-input/add-character % c)))))
       :on-delete            (fn []
                               (debounce/clear-all)
                               (set-input-state controlled-input/delete-last)
                               (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes]))
       :on-long-press-delete (fn []
                               (debounce/clear-all)
                               (set-input-state controlled-input/delete-all)
                               (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes]))}]]))


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
  [{:keys [amount bounce-duration-ms token valid-input?]}]
  (if valid-input?
    (debounce/debounce-and-dispatch
     [:wallet/get-suggested-routes
      {:amount        amount
       :updated-token token}]
     bounce-duration-ms)
    (rf/dispatch [:wallet/clean-suggested-routes])))

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
        bottom                                      (safe-area/get-bottom)
        [crypto-currency? set-crypto-currency]      (rn/use-state initial-crypto-currency?)
        on-navigate-back                            on-navigate-back
        [input-state set-input-state]               (rn/use-state controlled-input/init-state)
        [just-toggled-mode? set-just-toggled-mode?] (rn/use-state false)
        clear-input!                                #(set-input-state controlled-input/delete-all)
        handle-on-confirm                           (fn []
                                                      (rf/dispatch [:wallet/set-token-amount-to-send
                                                                    {:amount
                                                                     (controlled-input/input-value
                                                                      input-state)
                                                                     :stack-id current-screen-id}]))
        {fiat-currency :currency}                   (rf/sub [:profile/profile])
        {token-symbol :symbol
         token-networks :networks
         token-decimals :decimals
         :as
         token}                                     (rf/sub [:wallet/wallet-send-token])
        send-enabled-networks                       (rf/sub [:wallet/wallet-send-enabled-networks])
        enabled-from-chain-ids                      (rf/sub
                                                     [:wallet/wallet-send-enabled-from-chain-ids])
        send-from-locked-amounts                    (rf/sub [:wallet/wallet-send-from-locked-amounts])
        {token-balance     :total-balance
         available-balance :available-balance
         :as               token-by-symbol}         (rf/sub [:wallet/token-by-symbol
                                                             (str token-symbol)
                                                             enabled-from-chain-ids])
        currency-symbol                             (rf/sub [:profile/currency-symbol])
        currency                                    (rf/sub [:profile/currency])
        conversion-rate                             (-> token
                                                        :market-values-per-currency
                                                        currency
                                                        :price)
        loading-routes?                             (rf/sub
                                                     [:wallet/wallet-send-loading-suggested-routes?])
        route                                       (rf/sub [:wallet/wallet-send-route])
        on-confirm                                  (or default-on-confirm handle-on-confirm)
        crypto-decimals                             (or token-decimals default-crypto-decimals)
        current-crypto-limit                        (or default-limit-crypto
                                                        (utils/get-standard-crypto-format
                                                         token
                                                         token-balance))
        available-crypto-limit                      (or default-limit-crypto
                                                        (utils/get-standard-crypto-format
                                                         token
                                                         available-balance))
        current-fiat-limit                          (.toFixed (* token-balance conversion-rate) 2)
        available-fiat-limit                        (.toFixed (* available-balance conversion-rate) 2)
        current-limit                               (if crypto-currency?
                                                      current-crypto-limit
                                                      current-fiat-limit)
        available-limit                             (if crypto-currency?
                                                      available-crypto-limit
                                                      available-fiat-limit)
        valid-input?                                (not (or (string/blank?
                                                              (controlled-input/input-value
                                                               input-state))
                                                             (<= (controlled-input/numeric-value
                                                                  input-state)
                                                                 0)
                                                             (> (controlled-input/numeric-value
                                                                 input-state)
                                                                available-limit)))
        input-num-value                             (controlled-input/numeric-value input-state)
        input-amount                                (controlled-input/input-value input-state)
        confirm-disabled?                           (or (nil? route)
                                                        (empty? route)
                                                        (string/blank? (controlled-input/input-value
                                                                        input-state))
                                                        (<= input-num-value 0)
                                                        (> input-num-value current-limit))
        amount-in-crypto                            (if crypto-currency?
                                                      input-amount
                                                      (number/remove-trailing-zeroes
                                                       (.toFixed (/ input-amount conversion-rate)
                                                                 crypto-decimals)))
        send-amount                                 (rf/sub [:wallet/wallet-send-amount])
        amount-text                                 (str (number/remove-trailing-zeroes
                                                          (.toFixed (js/parseFloat send-amount)
                                                                    (min token-decimals 6)))
                                                         " "
                                                         token-symbol)
        first-route                                 (first route)
        native-currency-symbol                      (when-not confirm-disabled?
                                                      (get-in first-route
                                                              [:from :native-currency-symbol]))
        native-token                                (when native-currency-symbol
                                                      (rf/sub [:wallet/token-by-symbol
                                                               native-currency-symbol]))
        fee-formatted                               (rf/sub [:wallet/wallet-send-fee-fiat-formatted
                                                             native-token])
        show-select-asset-sheet                     #(rf/dispatch
                                                      [:show-bottom-sheet
                                                       {:content (fn []
                                                                   [select-asset-bottom-sheet
                                                                    clear-input!])}])
        sender-network-values                       (rf/sub
                                                     [:wallet/wallet-send-sender-network-values])
        receiver-network-values                     (rf/sub
                                                     [:wallet/wallet-send-receiver-network-values])
        tx-type                                     (rf/sub [:wallet/wallet-send-tx-type])
        token-not-supported-in-receiver-networks?   (and (not= tx-type :tx/bridge)
                                                         (->> receiver-network-values
                                                              (remove #(= (:type %) :add))
                                                              (every? #(= (:type %) :not-available))))
        suggested-routes                            (rf/sub [:wallet/wallet-send-suggested-routes])
        routes                                      (when suggested-routes
                                                      (or (:best suggested-routes) []))
        no-routes-found?                            (and
                                                     (every-network-value-is-zero?
                                                      sender-network-values)
                                                     (not (nil? routes))
                                                     (not loading-routes?)
                                                     (not token-not-supported-in-receiver-networks?))
        receiver-networks                           (rf/sub [:wallet/wallet-send-receiver-networks])
        receiver-preferred-networks                 (rf/sub
                                                     [:wallet/wallet-send-receiver-preferred-networks])
        receiver-preferred-networks-set             (set receiver-preferred-networks)
        sending-to-unpreferred-networks?            (not (every? (fn [receiver-selected-network]
                                                                   (contains?
                                                                    receiver-preferred-networks-set
                                                                    receiver-selected-network))
                                                                 receiver-networks))
        input-error                                 (controlled-input/input-error input-state)
        limit-insufficient?                         (> (controlled-input/numeric-value input-state)
                                                       current-limit)
        should-try-again?                           (and (not limit-insufficient?) no-routes-found?)
        current-address                             (rf/sub [:wallet/current-viewing-account-address])
        owned-eth-token                             (rf/sub [:wallet/token-by-symbol
                                                             (string/upper-case
                                                              constants/mainnet-short-name)
                                                             enabled-from-chain-ids])
        not-enough-asset?                           (and
                                                     (or no-routes-found? limit-insufficient?)
                                                     (not-empty sender-network-values)
                                                     (if (= token-symbol
                                                            (string/upper-case
                                                             constants/mainnet-short-name))
                                                       (= current-limit input-amount)
                                                       (money/equal-to (:total-balance
                                                                        owned-eth-token)
                                                                       0)))
        show-no-routes?                             (and
                                                     (or no-routes-found? limit-insufficient?)
                                                     (not-empty sender-network-values)
                                                     (not not-enough-asset?))
        request-fetch-routes                        (fn [bounce-duration-ms]
                                                      (fetch-routes
                                                       {:amount             amount-in-crypto
                                                        :valid-input?       valid-input?
                                                        :bounce-duration-ms bounce-duration-ms
                                                        :token              token}))
        swap-between-fiat-and-crypto                (fn [swap-to-crypto-currency?]
                                                      (set-just-toggled-mode? true)
                                                      (set-crypto-currency swap-to-crypto-currency?)
                                                      (set-input-state
                                                       (fn [input-state]
                                                         (controlled-input/set-input-value
                                                          input-state
                                                          (let [value     (controlled-input/input-value
                                                                           input-state)
                                                                new-value (if swap-to-crypto-currency?
                                                                            (.toFixed (/ value
                                                                                         conversion-rate)
                                                                                      crypto-decimals)
                                                                            (.toFixed (* value
                                                                                         conversion-rate)
                                                                                      12))]
                                                            (number/remove-trailing-zeroes
                                                             new-value))))))]
    (rn/use-mount
     (fn []
       (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
             app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
         #(.remove app-keyboard-listener))))
    (hot-reload/use-safe-unmount on-navigate-back)
    (rn/use-effect
     (fn []
       (set-input-state #(controlled-input/set-upper-limit % current-limit)))
     [current-limit])
    (rn/use-effect
     (fn []
       (when input-error (debounce/clear-all))
       (when (and limit-insufficient? routes) (rf/dispatch [:wallet/reset-network-amounts-to-zero])))
     [input-error])
    (rn/use-effect
     (fn []
       (clear-input!)
       (rf/dispatch [:wallet/clean-suggested-routes]))
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
      {:container-style  style/input-container
       :token            token-symbol
       :currency         fiat-currency
       :currency-symbol  currency-symbol
       :crypto-decimals  (min token-decimals 6)
       :error?           (controlled-input/input-error input-state)
       :networks         (seq send-enabled-networks)
       :title            (i18n/label
                          :t/send-limit
                          {:limit (if crypto-currency?
                                    (utils/make-limit-label-crypto current-limit token-symbol)
                                    (utils/make-limit-label-fiat current-limit currency-symbol))})
       :conversion       conversion-rate
       :show-keyboard?   false
       :value            input-amount
       :on-swap          swap-between-fiat-and-crypto
       :on-token-press   show-select-asset-sheet
       :allow-selection? false}]
     [routes/view
      {:token                                     token-by-symbol
       :send-amount-in-crypto                     amount-in-crypto
       :valid-input?                              valid-input?
       :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
       :lock-fetch-routes?                        just-toggled-mode?
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
       :button-one-props (merge (when-not should-try-again? button-one-props)
                                {:disabled? (or loading-routes?
                                                (and (not should-try-again?) confirm-disabled?))
                                 :on-press  (cond
                                              should-try-again?
                                              #(rf/dispatch [:wallet/get-suggested-routes
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
                               (let [new-text      (str input-amount c)
                                     max-decimals  (if crypto-currency? crypto-decimals 2)
                                     regex-pattern (str "^\\d*\\.?\\d{0," max-decimals "}$")
                                     regex         (re-pattern regex-pattern)]
                                 (when (re-matches regex new-text)
                                   (debounce/clear-all)
                                   (set-just-toggled-mode? false)
                                   (set-input-state #(controlled-input/add-character % c)))))
       :on-delete            (fn []
                               (debounce/clear-all)
                               (set-just-toggled-mode? false)
                               (set-input-state controlled-input/delete-last)
                               (rf/dispatch [:wallet/clean-suggested-routes]))
       :on-long-press-delete (fn []
                               (debounce/clear-all)
                               (set-just-toggled-mode? false)
                               (set-input-state controlled-input/delete-all)
                               (rf/dispatch [:wallet/clean-suggested-routes]))}]]))

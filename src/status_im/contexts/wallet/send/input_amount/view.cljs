(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.estimated-fees :as estimated-fees]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn select-asset-bottom-sheet
  [clear-input!]
  (let [{preselected-token-symbol :symbol} (rf/sub [:wallet/wallet-send-token])]
    [:<> ;; Need to be a `:<>` to keep `asset-list` scrollable.
     [quo/drawer-top
      {:title           (i18n/label :t/select-token)
       :container-style {:padding-bottom 8}}]
     [asset-list/view
      {:content-container-style  {:padding-horizontal 8
                                  :padding-bottom     8}
       :preselected-token-symbol preselected-token-symbol
       :on-token-press           (fn [token]
                                   (rf/dispatch [:wallet/edit-token-to-send token])
                                   (clear-input!))}]]))

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

(defn- insufficient-asset-amount?
  [{:keys [token-symbol owned-eth-token input-state limit-exceeded? enough-assets?]}]
  (let [eth-selected?              (= token-symbol (string/upper-case constants/mainnet-short-name))
        zero-owned-eth?            (money/equal-to (:total-balance owned-eth-token) 0)
        input-at-max-owned-amount? (money/equal-to
                                    (controlled-input/value-bn input-state)
                                    (controlled-input/upper-limit-bn input-state))
        exceeded-input?            (if eth-selected?
                                     input-at-max-owned-amount?
                                     zero-owned-eth?)]
    (or exceeded-input?
        limit-exceeded?
        (false? enough-assets?))))

(defn view
  ;; crypto-decimals, limit-crypto and initial-crypto-currency? args are needed
  ;; for component tests only
  [{default-on-confirm       :on-confirm
    default-limit-crypto     :limit-crypto
    on-navigate-back         :on-navigate-back
    button-one-label         :button-one-label
    button-one-props         :button-one-props
    current-screen-id        :current-screen-id
    initial-crypto-currency? :initial-crypto-currency?
    :or                      {initial-crypto-currency? true}}]
  (let [view-id                       (rf/sub [:view-id])
        {fiat-currency :currency}     (rf/sub [:profile/profile])
        currency                      (rf/sub [:profile/currency])
        {token-symbol :symbol
         :as          token}          (rf/sub [:wallet/wallet-send-token])
        network                       (rf/sub [:wallet/send-network])
        {:keys [total-balance]
         :as   token-by-symbol}       (rf/sub [:wallet/token-by-symbol
                                               (str token-symbol)
                                               [(:chain-id network)]])
        currency-symbol               (rf/sub [:profile/currency-symbol])
        loading-routes?               (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        route                         (rf/sub [:wallet/wallet-send-route])
        current-address               (rf/sub [:wallet/current-viewing-account-address])
        current-color                 (rf/sub [:wallet/current-viewing-account-color])
        enough-assets?                (rf/sub [:wallet/wallet-send-enough-assets?])
        owned-eth-token               (rf/sub [:wallet/token-by-symbol
                                               (string/upper-case constants/mainnet-short-name)
                                               [(:chain-id network)]])
        suggested-routes              (rf/sub [:wallet/wallet-send-suggested-routes])
        tx-type                       (rf/sub [:wallet/wallet-send-tx-type])
        token-decimals                (rf/sub [:wallet/send-display-token-decimals])
        prices-per-token              (rf/sub [:wallet/prices-per-token])
        [crypto-currency?
         set-crypto-currency]         (rn/use-state initial-crypto-currency?)
        [input-state set-input-state] (rn/use-state controlled-input/init-state)
        active-screen?                (= view-id current-screen-id)
        bottom                        (safe-area/get-bottom)
        token-balance                 (or default-limit-crypto total-balance)
        usd-conversion-rate           (utils/token-usd-price token prices-per-token)
        conversion-rate               (utils/token-price-by-symbol prices-per-token
                                                                   token-symbol
                                                                   currency)
        clear-input!                  #(set-input-state controlled-input/delete-all)
        max-limit                     (if crypto-currency?
                                        (utils/cut-crypto-decimals-to-fit-usd-cents
                                         token-balance
                                         usd-conversion-rate)
                                        (utils/cut-fiat-balance-to-two-decimals
                                         (money/crypto->fiat token-balance conversion-rate)))
        input-value                   (controlled-input/input-value input-state)
        valid-input?                  (not (or (controlled-input/empty-value? input-state)
                                               (controlled-input/input-error input-state)))
        amount-in-crypto              (if crypto-currency?
                                        input-value
                                        (rf/sub [:wallet/send-amount-fixed
                                                 (/ input-value conversion-rate)]))
        routes-request-uuid           (when suggested-routes
                                        (:uuid suggested-routes))
        no-routes-found?              (and
                                       routes-request-uuid
                                       (empty? route)
                                       (not loading-routes?))
        input-error                   (controlled-input/input-error input-state)
        limit-exceeded?               (controlled-input/upper-limit-exceeded? input-state)
        not-enough-asset?             (insufficient-asset-amount?
                                       {:enough-assets?  enough-assets?
                                        :token-symbol    token-symbol
                                        :owned-eth-token owned-eth-token
                                        :input-state     input-state
                                        :limit-exceeded? limit-exceeded?})
        should-try-again?             (and (not limit-exceeded?)
                                           no-routes-found?
                                           (not not-enough-asset?))
        show-no-routes?               (and (or no-routes-found? limit-exceeded?)
                                           (not not-enough-asset?))
        confirm-disabled?             (or (nil? route)
                                          (empty? route)
                                          (not valid-input?))
        fee-formatted                 (when (or (not confirm-disabled?) not-enough-asset?)
                                        (get-fee-formatted route))
        handle-on-confirm             (fn [amount]
                                        (rf/dispatch [:wallet/set-token-amount-to-send
                                                      {:amount   amount
                                                       :stack-id current-screen-id}]))
        on-confirm                    (or default-on-confirm handle-on-confirm)
        show-select-asset-sheet       #(rf/dispatch
                                        [:show-bottom-sheet
                                         {:content (fn []
                                                     [select-asset-bottom-sheet
                                                      clear-input!])}])
        request-fetch-routes          (fn [bounce-duration-ms]
                                        (fetch-routes
                                         {:amount                 amount-in-crypto
                                          :valid-input?           valid-input?
                                          :bounce-duration-ms     bounce-duration-ms
                                          :token                  token
                                          :reset-amounts-to-zero? (and limit-exceeded?
                                                                       (some? route))}))
        swap-between-fiat-and-crypto  (fn []
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
       (rf/dispatch [:wallet/stop-and-clean-suggested-routes]))
     [current-address])
    (rn/use-effect
     (fn []
       (when active-screen?
         (request-fetch-routes 2000)))
     [amount-in-crypto valid-input?])
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
                         {:networks (seq [network])
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
     (if (= tx-type :tx/bridge)
       [routes/view]
       [rn/view {:style {:flex 1}}])
     (when not-enough-asset?
       [not-enough-asset])
     (when (or loading-routes? route)
       [estimated-fees/view
        {:not-enough-asset? not-enough-asset?
         :loading-routes?   loading-routes?
         :fees              fee-formatted}])
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
                                 :on-press            (if should-try-again?
                                                        #(rf/dispatch [:wallet/start-get-suggested-routes
                                                                       {:amount        amount-in-crypto
                                                                        :updated-token token-by-symbol}])
                                                        #(on-confirm amount-in-crypto))
                                 :customization-color current-color}
                                (when should-try-again?
                                  {:type :grey}))}]
     [quo/numbered-keyboard
      {:container-style      (style/keyboard-container bottom)
       :left-action          :dot
       :delete-key?          true
       :on-press             (fn [c]
                               (let [new-text      (str input-value c)
                                     max-decimals  (if crypto-currency? token-decimals 2)
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


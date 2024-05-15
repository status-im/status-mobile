(ns status-im.contexts.wallet.send.input-amount.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.style :as style]
    [status-im.contexts.wallet.send.routes.view :as routes]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.contexts.wallet.sheets.unpreferred-networks-alert.view :as unpreferred-networks-alert]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn- make-limit-label
  [amount currency]
  (str amount
       " "
       (some-> currency
               name
               string/upper-case)))

(defn- estimated-fees
  [{:keys [loading-routes? fees amount receiver]}]
  [rn/view {:style style/estimated-fees-container}
   [rn/view {:style style/estimated-fees-content-container}
    [quo/button
     {:icon-only?          true
      :type                :outline
      :size                32
      :inner-style         {:opacity 1}
      :accessibility-label :advanced-button
      :disabled?           loading-routes?
      :on-press            #(js/alert "Not implemented yet")}
     :i/advanced]]
   [quo/data-item
    {:container-style style/fees-data-item
     :label           :none
     :status          (if loading-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/fees)
     :subtitle        fees}]
   [quo/data-item
    {:container-style style/amount-data-item
     :label           :none
     :status          (if loading-routes? :loading :default)
     :size            :small
     :title           (i18n/label :t/user-gets {:name receiver})
     :subtitle        amount}]])

(defn- every-network-value-is-zero?
  [sender-network-values]
  (every? (fn [{:keys [total-amount]}]
            (and
             total-amount
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
        :color colors/danger-50}]]
     [rn/view {:style style/token-not-available-content-container}
      [quo/text
       {:style (style/token-not-available-text theme)
        :size  :paragraph-2}
       (i18n/label :t/token-not-available-on-receiver-networks {:token-symbol token-symbol})]
      [quo/button
       {:size                24
        :customization-color colors/danger-50
        :on-press            add-token-networks}
       (i18n/label :t/add-networks-token-can-be-sent-to {:token-symbol token-symbol})]]]))

(defn- show-unpreferred-networks-alert
  [on-confirm]
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn []
                [unpreferred-networks-alert/view
                 {:on-confirm on-confirm}])}]))
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
        bottom           (safe-area/get-bottom)
        crypto-currency? (reagent/atom initial-crypto-currency?)
        on-navigate-back on-navigate-back]
    (fn []
      (let [[input-state set-input-state]             (rn/use-state controlled-input/init-state)
            clear-input!                              #(set-input-state controlled-input/delete-all)
            handle-on-confirm                         (fn []
                                                        (rf/dispatch [:wallet/set-token-amount-to-send
                                                                      {:amount
                                                                       (controlled-input/input-value
                                                                        input-state)
                                                                       :stack-id current-screen-id}]))
            {fiat-currency :currency}                 (rf/sub [:profile/profile])
            {token-symbol   :symbol
             token-networks :networks}                (rf/sub [:wallet/wallet-send-token])
            {token-balance :total-balance
             :as
             token}                                   (rf/sub
                                                       [:wallet/current-viewing-account-tokens-filtered
                                                        (str token-symbol)])
            conversion-rate                           (-> token :market-values-per-currency :usd :price)
            loading-routes?                           (rf/sub
                                                       [:wallet/wallet-send-loading-suggested-routes?])

            route                                     (rf/sub [:wallet/wallet-send-route])
            to-address                                (rf/sub [:wallet/wallet-send-to-address])

            on-confirm                                (or default-on-confirm handle-on-confirm)
            crypto-decimals                           (or default-crypto-decimals
                                                          (utils/get-crypto-decimals-count token))
            crypto-limit                              (or default-limit-crypto
                                                          (utils/get-standard-crypto-format
                                                           token
                                                           token-balance))
            fiat-limit                                (.toFixed (* token-balance conversion-rate) 2)
            current-limit                             (if @crypto-currency? crypto-limit fiat-limit)
            valid-input?                              (not (or (string/blank?
                                                                (controlled-input/input-value
                                                                 input-state))
                                                               (<= (controlled-input/numeric-value
                                                                    input-state)
                                                                   0)
                                                               (> (controlled-input/numeric-value
                                                                   input-state)
                                                                  current-limit)))
            current-currency                          (if @crypto-currency? token-symbol fiat-currency)
            input-num-value                           (controlled-input/numeric-value input-state)
            confirm-disabled?                         (or (nil? route)
                                                          (empty? route)
                                                          (string/blank? (controlled-input/input-value
                                                                          input-state))
                                                          (<= input-num-value 0)
                                                          (> input-num-value current-limit))
            amount-text                               (str (controlled-input/input-value input-state)
                                                           " "
                                                           token-symbol)
            first-route                               (first route)
            native-currency-symbol                    (when-not confirm-disabled?
                                                        (get-in first-route
                                                                [:from :native-currency-symbol]))
            native-token                              (when native-currency-symbol
                                                        (rf/sub [:wallet/token-by-symbol
                                                                 native-currency-symbol]))
            fee-in-native-token                       (when-not confirm-disabled?
                                                        (send-utils/calculate-full-route-gas-fee route))
            fee-in-crypto-formatted                   (when fee-in-native-token
                                                        (utils/get-standard-crypto-format
                                                         native-token
                                                         fee-in-native-token))
            fee-in-fiat                               (when-not confirm-disabled?
                                                        (utils/calculate-token-fiat-value
                                                         {:currency fiat-currency
                                                          :balance  fee-in-native-token
                                                          :token    native-token}))
            currency-symbol                           (rf/sub [:profile/currency-symbol])
            fee-formatted                             (when fee-in-fiat
                                                        (utils/get-standard-fiat-format
                                                         fee-in-crypto-formatted
                                                         currency-symbol
                                                         fee-in-fiat))
            show-select-asset-sheet                   #(rf/dispatch
                                                        [:show-bottom-sheet
                                                         {:content (fn []
                                                                     [select-asset-bottom-sheet
                                                                      clear-input!])}])
            sender-network-values                     (rf/sub
                                                       [:wallet/wallet-send-sender-network-values])
            receiver-network-values                   (rf/sub
                                                       [:wallet/wallet-send-receiver-network-values])
            token-not-supported-in-receiver-networks? (every? #(= (:type %) :not-available)
                                                              (filter #(not= (:type %) :add)
                                                                      receiver-network-values))
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
                                                                   receiver-networks))]
        (rn/use-mount
         (fn []
           (let [dismiss-keyboard-fn   #(when (= % "active") (rn/dismiss-keyboard!))
                 app-keyboard-listener (.addEventListener rn/app-state "change" dismiss-keyboard-fn)]
             #(.remove app-keyboard-listener))))
        (rn/use-effect
         (fn []
           (set-input-state #(controlled-input/set-upper-limit % current-limit)))
         [current-limit])
        [rn/view
         {:style               style/screen
          :accessibility-label (str "container"
                                    (when (controlled-input/input-error input-state) "-error"))}
         [account-switcher/view
          {:icon-name     :i/arrow-left
           :on-press      on-navigate-back
           :switcher-type :select-account}]
         [quo/token-input
          {:container-style style/input-container
           :token           token-symbol
           :currency        current-currency
           :crypto-decimals crypto-decimals
           :error?          (controlled-input/input-error input-state)
           :networks        (seq token-networks)
           :title           (i18n/label :t/send-limit
                                        {:limit (make-limit-label current-limit current-currency)})
           :conversion      conversion-rate
           :show-keyboard?  false
           :value           (controlled-input/input-value input-state)
           :on-swap         #(reset! crypto-currency? %)
           :on-token-press  show-select-asset-sheet}]
         [routes/view
          {:token                                     token
           :input-value                               (controlled-input/input-value input-state)
           :valid-input?                              valid-input?
           :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
           :current-screen-id                         current-screen-id}]
         (when (and (not loading-routes?)
                    sender-network-values
                    token-not-supported-in-receiver-networks?)
           [token-not-available token-symbol receiver-networks token-networks])
         (when (or loading-routes? (seq route))
           [estimated-fees
            {:loading-routes? loading-routes?
             :fees            fee-formatted
             :amount          amount-text
             :receiver        (address/get-shortened-key to-address)}])
         (when no-routes-found?
           [rn/view {:style style/no-routes-found-container}
            [quo/info-message
             {:type  :error
              :icon  :i/alert
              :size  :default
              :style {:margin-top 15}}
             (i18n/label :t/no-routes-found)]])
         [quo/bottom-actions
          {:actions          :one-action
           :button-one-label (if no-routes-found?
                               (i18n/label :t/try-again)
                               button-one-label)
           :button-one-props (merge button-one-props
                                    {:disabled? (and (not no-routes-found?) confirm-disabled?)
                                     :on-press  (cond
                                                  no-routes-found?
                                                  #(rf/dispatch [:wallet/get-suggested-routes
                                                                 {:amount (controlled-input/input-value
                                                                           input-state)}])
                                                  sending-to-unpreferred-networks?
                                                  #(show-unpreferred-networks-alert on-confirm)
                                                  :else
                                                  on-confirm)}
                                    (when no-routes-found?
                                      {:type :grey}))}]
         [quo/numbered-keyboard
          {:container-style      (style/keyboard-container bottom)
           :left-action          :dot
           :delete-key?          true
           :on-press             (fn [c]
                                   (when-not loading-routes?
                                     (set-input-state #(controlled-input/add-character % c))))
           :on-delete            (fn []
                                   (when-not loading-routes?
                                     (set-input-state controlled-input/delete-last)))
           :on-long-press-delete (fn []
                                   (when-not loading-routes?
                                     (set-input-state controlled-input/delete-all)))}]]))))

(ns status-im.contexts.wallet.swap.swap-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.swap.swap-confirmation.style :as style]
    [utils.address :as address-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-close-action
  []
  (rf/dispatch [:navigate-back]))

(defn- swap-title
  [{:keys [pay-token-symbol pay-amount receive-token-symbol receive-amount account]}]
  [rn/view {:style style/content-container}
   [rn/view {:style {:flex-direction :row}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :title-label}
     (i18n/label :t/swap)]
    [quo/summary-tag
     {:token pay-token-symbol
      :label (str pay-amount " " pay-token-symbol)
      :type  :token}]]
   [rn/view {:style style/title-line-with-margin-top}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :title-label}
     (i18n/label :t/to)]
    [quo/summary-tag
     {:token receive-token-symbol
      :label (str receive-amount " " receive-token-symbol)
      :type  :token}]]
   [rn/view {:style style/title-line-with-margin-top}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/in)]
    [quo/summary-tag
     {:label               (:name account)
      :type                :account
      :emoji               (:emoji account)
      :customization-color (:color account)}]]])

(defn- summary-section
  [{:keys [theme label title-accessibility-label amount token-symbol token-address network]}]
  (let [network-values {(if (= network :mainnet) :ethereum network)
                        {:amount amount :token-symbol token-symbol}}]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label title-accessibility-label}
      label]
     [quo/summary-info
      {:type        :token
       :networks?   true
       :values      network-values
       :token-props {:token   token-symbol
                     :label   (str amount " " token-symbol)
                     :address (address-utils/get-shortened-compressed-key token-address)
                     :size    32}}]]))

(defn- data-item
  [{:keys [title subtitle loading?]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :status          (if loading? :loading :default)
    :size            :small
    :title           title
    :subtitle        subtitle}])

(defn- transaction-details
  [{:keys [estimated-time-min max-fees max-slippage loading-fees?]}]
  [rn/view {:style style/details-container}
   [:<>
    [data-item
     {:title    (i18n/label :t/est-time)
      :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
    [data-item
     {:title    (i18n/label :t/max-fees)
      :subtitle max-fees
      :loading? loading-fees?}]
    [data-item
     {:title    (i18n/label :t/max-slippage)
      :subtitle (str max-slippage "%")}]]])

(defn footer
  [{:keys [estimated-time-min native-currency-symbol max-slippage theme account-color provider
           loading-fees?]}]
  (let [fee-formatted (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol])]
    [:<>
     [transaction-details
      {:estimated-time-min estimated-time-min
       :max-fees           fee-formatted
       :max-slippage       max-slippage
       :loading-fees?      loading-fees?
       :theme              theme}]
     [standard-auth/slide-button
      {:size                :size-48
       :track-text          (i18n/label :t/slide-to-swap)
       :container-style     {:z-index 2}
       :customization-color account-color
       :disabled?           loading-fees?
       :auth-button-label   (i18n/label :t/confirm)}]
     [rn/view {:style style/providers-container}
      [quo/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/neutral-80-opa-40
                                            colors/white-opa-70
                                            theme)}}
       (i18n/label :t/swaps-powered-by {:provider (:name provider)})]]]))

(defn view
  []
  (let [theme                   (quo.theme/use-theme)
        swap-transaction-data   (rf/sub [:wallet/swap])
        {:keys [asset-to-pay max-slippage network
                pay-amount providers swap-proposal
                loading-fees?]} swap-transaction-data
        receive-amount          (:receive-amount swap-proposal)
        receive-token           (:receive-token swap-proposal)
        receive-token-symbol    (:symbol receive-token)
        receive-token-address   (:address receive-token)
        estimated-time-min      (:estimated-time swap-proposal)
        pay-token-symbol        (:symbol asset-to-pay)
        pay-token-address       (:address asset-to-pay)
        native-currency-symbol  (get-in swap-proposal [:from :native-currency-symbol])
        account                 (rf/sub [:wallet/current-viewing-account])
        account-color           (:color account)
        provider                (first providers)]
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/arrow-left
                                   :on-press            on-close-action
                                   :margin-top          (safe-area/get-top)
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [footer
                                  {:estimated-time-min     estimated-time-min
                                   :native-currency-symbol native-currency-symbol
                                   :max-slippage           max-slippage
                                   :account-color          account-color
                                   :provider               provider
                                   :loading-fees?          loading-fees?
                                   :theme                  theme}]
       :gradient-cover?          true
       :customization-color      account-color}
      [rn/view
       [swap-title
        {:pay-token-symbol     pay-token-symbol
         :pay-amount           pay-amount
         :receive-token-symbol receive-token-symbol
         :receive-amount       receive-amount
         :account              account}]
       [summary-section
        {:title-accessibility-label :summary-section-pay
         :label                     (i18n/label :t/pay)
         :token-symbol              pay-token-symbol
         :amount                    pay-amount
         :token-address             pay-token-address
         :network                   (:network-name network)
         :theme                     theme}]
       [summary-section
        {:title-accessibility-label :summary-section-receive
         :label                     (i18n/label :t/receive)
         :token-symbol              receive-token-symbol
         :amount                    receive-amount
         :token-address             receive-token-address
         :network                   (:network-name network)
         :theme                     theme}]]]]))

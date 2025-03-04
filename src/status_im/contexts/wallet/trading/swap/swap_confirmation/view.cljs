(ns status-im.contexts.wallet.trading.swap.swap-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.swap.swap-confirmation.style :as style]
    [status-im.contexts.wallet.trading.swap.components.info-bar.view :as info-bar]
    [status-im.contexts.wallet.trading.swap.components.transaction.container :as transaction.container]
    [status-im.contexts.wallet.trading.swap.components.transaction.provider-footer :as
     transaction.provider-footer]
    [status-im.contexts.wallet.trading.swap.components.transaction.title :as transaction.title]
    [utils.address :as address-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-confirm
  [password]
  (rf/dispatch [:dismiss-modal :screen/trading.swap-confirmation])
  (rf/dispatch [:wallet.routes/sign-transactions password]))

(defn- swap-title
  []
  (let [pay-token-symbol     (rf/sub [:trading.swap/pay-token-symbol])
        receive-token-symbol (rf/sub [:trading.swap/receive-token-symbol])
        pay-amount           (rf/sub [:trading.swap/pay-amount])
        receive-amount       (rf/sub [:trading.swap/formatted-receive-amount])
        account              (rf/sub [:trading.swap/account])]
    [transaction.title/container
     [transaction.title/row {:first? true}
      [transaction.title/text
       {:label               (i18n/label :t/swap)
        :accessibility-label :title-label}]
      [quo/summary-tag
       {:token pay-token-symbol
        :label (str pay-amount " " pay-token-symbol)
        :type  :token}]]
     [transaction.title/row
      [transaction.title/text
       {:label               (i18n/label :t/to)
        :accessibility-label :title-label}]
      [quo/summary-tag
       {:token receive-token-symbol
        :label (str receive-amount " " receive-token-symbol)
        :type  :token}]]
     [transaction.title/row
      [transaction.title/text
       {:label               (i18n/label :t/in)
        :accessibility-label :send-label}]
      [quo/summary-tag
       {:label               (:name account)
        :type                :account
        :emoji               (:emoji account)
        :customization-color (:color account)}]]]))

(defn- summary-container
  [{:keys [title accessibility-label]} & children]
  (let [theme (quo.theme/use-theme)]
    (into [rn/view {:style style/summary-section-container}
           [quo/text
            {:size                :paragraph-2
             :weight              :medium
             :style               (style/section-label theme)
             :accessibility-label accessibility-label}
            title]]
          children)))

(defn- format-token-address
  [address]
  (when-not (address-utils/zero-address? address)
    (address-utils/get-shortened-compressed-key address)))

(defn- pay-section
  []
  (let [pay-token         (rf/sub [:trading.swap/pay-token])
        pay-amount        (rf/sub [:trading.swap/pay-amount])
        pay-token-symbol  (:symbol pay-token)
        pay-token-address (:address pay-token)]
    [summary-container
     {:title               (i18n/label :t/pay)
      :accessibility-label :summary-section-pay}
     [quo/summary-info
      {:type        :token
       :token-props {:token   pay-token-symbol
                     :label   (str pay-amount " " pay-token-symbol)
                     :address (format-token-address pay-token-address)
                     :size    32}}]]))

(defn- receive-section
  []
  (let [receive-token         (rf/sub [:trading.swap/receive-token])
        receive-amount        (rf/sub [:trading.swap/receive-amount])
        receive-token-symbol  (:symbol receive-token)
        receive-token-address (:address receive-token)]
    [summary-container
     {:title               (i18n/label :t/receive)
      :accessibility-label :summary-section-receive}
     [quo/summary-info
      {:type        :token
       :token-props {:token   receive-token-symbol
                     :label   (str receive-amount " " receive-token-symbol)
                     :address (format-token-address receive-token-address)
                     :size    32}}]]))

(defn- network-section
  []
  (let [{:keys [full-name source]} (rf/sub [:trading.swap/network])]
    [summary-container
     {:title               (i18n/label :t/on)
      :accessibility-label :summary-section-network}
     [quo/summary-info
      {:type          :network
       :network-props {:label  full-name
                       :source source
                       :size   32}}]]))

(defn- transaction-details
  []
  (let [max-fees       (rf/sub [:trading.swap/total-fee-fiat])
        estimated-time (rf/sub [:trading.swap/estimated-time])
        max-slippage   (rf/sub [:trading.swap/max-slippage])]
    [info-bar/container {:style {:padding-top 7 :margin-bottom 8}}
     [info-bar/estimated-time {:estimated-time-mins estimated-time}]
     [info-bar/fiat-max-fee {:fee-amount max-fees}]
     [info-bar/max-slippage {:slippage max-slippage}]]))

(defn- slide-button
  []
  (let [transaction-for-signing (rf/sub [:wallet.routes/transactions-for-signing])
        route                   (rf/sub [:trading.swap/route])
        customization-color     (rf/sub [:trading.swap/customization-color])
        sign-on-keycard?        (get-in transaction-for-signing
                                        [:signingDetails :signOnKeycard])]
    [standard-auth/slide-button
     {:size                :size-48
      :track-text          (i18n/label :t/slide-to-swap)
      :container-style     {:z-index 2}
      :customization-color customization-color
      :disabled?           (or (not route)
                               (not transaction-for-signing))
      :auth-button-label   (i18n/label :t/confirm)
      :on-complete         (when sign-on-keycard? #(on-confirm ""))
      :on-auth-success     on-confirm}]))

(defn footer
  []
  [:<>
   [transaction-details]
   [slide-button]
   [transaction.provider-footer/view]])

(defn view
  []
  [transaction.container/view {:footer footer}
   [swap-title]
   [pay-section]
   [receive-section]
   [network-section]])

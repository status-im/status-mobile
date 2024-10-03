(ns status-im.contexts.wallet.swap.swap-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [status-im.contexts.wallet.swap.swap-confirmation.style :as style]
    [utils.address :as address-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- on-close-action
  []
  (rf/dispatch [:navigate-back]))

(defn- swap-title
  []
  (let [asset-to-pay         (rf/sub [:wallet/swap-asset-to-pay])
        asset-to-receive     (rf/sub [:wallet/swap-asset-to-receive])
        account              (rf/sub [:wallet/current-viewing-account])
        receive-amount       (rf/sub [:wallet/swap-receive-amount])
        pay-amount           (rf/sub [:wallet/swap-pay-amount])
        pay-token-symbol     (:symbol asset-to-pay)
        receive-token-symbol (:symbol asset-to-receive)]
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
        :customization-color (:color account)}]]]))

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
       :values      (send-utils/network-values-for-ui network-values)
       :token-props {:token   token-symbol
                     :label   (str amount " " token-symbol)
                     :address (address-utils/get-shortened-compressed-key token-address)
                     :size    32}}]]))

(defn- pay-section
  []
  (let [theme             (quo.theme/use-theme)
        asset-to-pay      (rf/sub [:wallet/swap-asset-to-pay])
        network           (rf/sub [:wallet/swap-network])
        pay-amount        (rf/sub [:wallet/swap-pay-amount])
        network-chain-id  (:chain-id network)
        network-name      (:network-name network)
        pay-token-symbol  (:symbol asset-to-pay)
        pay-token-address (get-in asset-to-pay [:balances-per-chain network-chain-id :address])]
    [summary-section
     {:title-accessibility-label :summary-section-pay
      :label                     (i18n/label :t/pay)
      :token-symbol              pay-token-symbol
      :amount                    pay-amount
      :token-address             pay-token-address
      :network                   network-name
      :theme                     theme}]))

(defn- receive-section
  []
  (let [theme                 (quo.theme/use-theme)
        asset-to-receive      (rf/sub [:wallet/swap-asset-to-receive])
        network               (rf/sub [:wallet/swap-network])
        receive-amount        (rf/sub [:wallet/swap-receive-amount])
        network-chain-id      (:chain-id network)
        network-name          (:network-name network)
        receive-token-symbol  (:symbol asset-to-receive)
        receive-token-address (get-in asset-to-receive [:balances-per-chain network-chain-id :address])]
    [summary-section
     {:title-accessibility-label :summary-section-receive
      :label                     (i18n/label :t/receive)
      :token-symbol              receive-token-symbol
      :amount                    receive-amount
      :token-address             receive-token-address
      :network                   network-name
      :theme                     theme}]))

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
  []
  (let [max-fees               (rf/sub [:wallet/wallet-swap-proposal-fee-fiat-formatted
                                        constants/token-for-fees-symbol])
        estimated-time         (rf/sub [:wallet/swap-proposal-estimated-time])
        loading-swap-proposal? (rf/sub [:wallet/swap-loading-swap-proposal?])
        max-slippage           (rf/sub [:wallet/swap-max-slippage])]
    [rn/view {:style style/details-container}
     [:<>
      [data-item
       {:title    (i18n/label :t/est-time)
        :subtitle (if estimated-time
                    (i18n/label :t/time-in-mins {:minutes (str estimated-time)})
                    (i18n/label :t/unknown))
        :loading? loading-swap-proposal?}]
      [data-item
       {:title    (i18n/label :t/max-fees)
        :subtitle (if (and estimated-time max-fees) max-fees (i18n/label :t/unknown))
        :loading? loading-swap-proposal?}]
      [data-item
       {:title    (i18n/label :t/max-slippage)
        :subtitle (str max-slippage "%")}]]]))

(defn- slide-button
  []
  (let [loading-swap-proposal? (rf/sub [:wallet/swap-loading-swap-proposal?])
        swap-proposal          (rf/sub [:wallet/swap-proposal-without-fees])
        account                (rf/sub [:wallet/current-viewing-account])
        account-color          (:color account)
        on-auth-success        (rn/use-callback (fn [data]
                                                  (rf/dispatch [:wallet/stop-get-swap-proposal])
                                                  (rf/dispatch [:wallet/swap-transaction
                                                                (security/safe-unmask-data data)])))]
    [standard-auth/slide-button
     {:size                :size-48
      :track-text          (i18n/label :t/slide-to-swap)
      :container-style     {:z-index 2}
      :customization-color account-color
      :disabled?           (or loading-swap-proposal? (not swap-proposal))
      :auth-button-label   (i18n/label :t/confirm)
      :on-auth-success     on-auth-success}]))

(defn footer
  []
  (let [provider (rf/sub [:wallet/swap-proposal-provider])
        theme    (quo.theme/use-theme)
        on-press (rn/use-callback #(when provider
                                     (rf/dispatch [:open-url (:terms-and-conditions-url provider)]))
                                  [provider])]
    [:<>
     [transaction-details]
     [slide-button]
     [rn/view {:style style/providers-container}
      [quo/text
       {:size  :paragraph-2
        :style (style/swaps-powered-by theme)}
       (i18n/label :t/swaps-powered-by
                   {:provider (if provider (:full-name provider) (i18n/label :t/unknown))})]
      [quo/text
       {:size     :paragraph-2
        :style    (style/terms-and-conditions theme)
        :on-press on-press}
       (i18n/label :t/terms-and-conditions)]]]))

(defn view
  []
  (let [account       (rf/sub [:wallet/current-viewing-account])
        account-color (:color account)]
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/arrow-left
                                   :on-press            on-close-action
                                   :margin-top          (safe-area/get-top)
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [footer]
       :gradient-cover?          true
       :customization-color      account-color}
      [rn/view
       [swap-title]
       [pay-section]
       [receive-section]]]]))

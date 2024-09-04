(ns status-im.contexts.wallet.swap.set-spending-cap.view
  (:require
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils.external-links :as external-links]
    [status-im.contexts.wallet.swap.set-spending-cap.style :as style]
    [utils.address :as address-utils]
    [utils.hex :as hex]
    [utils.i18n :as i18n]
    [utils.number :as number]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- swap-title
  []
  (let [asset-to-pay       (rf/sub [:wallet/swap-asset-to-pay])
        amount-in          (rf/sub [:wallet/swap-proposal-amount-in])
        account            (rf/sub [:wallet/current-viewing-account])
        provider           (rf/sub [:wallet/swap-proposal-provider])
        pay-token-symbol   (:symbol asset-to-pay)
        pay-token-decimals (:decimals asset-to-pay)
        pay-amount         (when amount-in
                             (number/convert-to-whole-number
                              (native-module/hex-to-number
                               (hex/normalize-hex
                                amount-in))
                              pay-token-decimals))]
    [rn/view {:style style/content-container}
     [rn/view {:style {:flex-direction :row}}
      [quo/text
       {:size                :heading-1
        :weight              :semi-bold
        :style               style/title-container
        :accessibility-label :set-spending-cap-of}
       (i18n/label :t/set-spending-cap-of)]]
     [rn/view {:style style/title-line-with-margin-top}
      [quo/summary-tag
       {:token pay-token-symbol
        :label (str pay-amount " " pay-token-symbol)
        :type  :token}]
      [quo/text
       {:size                :heading-1
        :weight              :semi-bold
        :style               style/title-container
        :accessibility-label :for}
       (i18n/label :t/for)]]
     [rn/view {:style style/title-line-with-margin-top}
      [quo/summary-tag
       {:label               (:full-name provider)
        :type                :network
        :image-source        (resources/get-network (:name provider))
        :customization-color (:color provider)}]
      [quo/text
       {:size                :heading-1
        :weight              :semi-bold
        :style               style/title-container
        :accessibility-label :on}
       (i18n/label :t/on)]]
     [rn/view {:style style/title-line-with-margin-top}
      [quo/summary-tag
       {:label               (:name account)
        :type                :account
        :emoji               (:emoji account)
        :customization-color (:color account)}]]]))

(defn- spending-cap-section
  []
  (let [theme              (quo.theme/use-theme)
        asset-to-pay       (rf/sub [:wallet/swap-asset-to-pay])
        amount-in          (rf/sub [:wallet/swap-proposal-amount-in])
        pay-token-symbol   (:symbol asset-to-pay)
        pay-token-decimals (:decimals asset-to-pay)
        pay-amount         (when amount-in
                             (number/convert-to-whole-number
                              (native-module/hex-to-number
                               (hex/normalize-hex
                                amount-in))
                              pay-token-decimals))]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :spending-cap-label}
      (i18n/label :t/spending-cap)]
     [quo/approval-info
      {:type            :spending-cap
       :unlimited-icon? false
       :label           (str pay-amount " " pay-token-symbol)
       :avatar-props    {:token pay-token-symbol}}]]))

(defn- account-section
  []
  (let [theme              (quo.theme/use-theme)
        asset-to-pay       (rf/sub [:wallet/swap-asset-to-pay])
        amount-in          (rf/sub [:wallet/swap-proposal-amount-in])
        account            (rf/sub [:wallet/current-viewing-account])
        pay-token-symbol   (:symbol asset-to-pay)
        pay-token-decimals (:decimals asset-to-pay)
        pay-amount         (when amount-in
                             (number/convert-to-whole-number
                              (native-module/hex-to-number
                               (hex/normalize-hex
                                amount-in))
                              pay-token-decimals))]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :account-label}
      (i18n/label :t/account)]
     [quo/approval-info
      {:type            :account
       :unlimited-icon? false
       :label           (:name account)
       :description     (address-utils/get-short-wallet-address (:address account))
       :tag-label       (str pay-amount " " pay-token-symbol)
       :avatar-props    {:emoji               (:emoji account)
                         :customization-color (:color account)}}]]))

(defn- on-option-press
  [{:keys [chain-id contract-address]}]
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn []
                [quo/action-drawer
                 [[{:icon                :i/link
                    :accessibility-label :view-on-etherscan
                    :on-press            (fn []
                                           (rf/dispatch
                                            [:wallet/navigate-to-chain-explorer-from-bottom-sheet
                                             (external-links/get-explorer-url-by-chain-id chain-id)
                                             contract-address]))
                    :label               (i18n/label :t/view-on-eth)
                    :right-icon          :i/external}]]])}]))

(defn- token-section
  []
  (let [theme             (quo.theme/use-theme)
        asset-to-pay      (rf/sub [:wallet/swap-asset-to-pay])
        network           (rf/sub [:wallet/swap-network])
        pay-token-symbol  (:symbol asset-to-pay)
        network-chain-id  (:chain-id network)
        pay-token-address (get-in asset-to-pay [:balances-per-chain network-chain-id :address])]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :token-label}
      (i18n/label :t/token)]
     [quo/approval-info
      {:type            :token-contract
       :option-icon     :i/options
       :on-option-press #(on-option-press {:chain-id         network-chain-id
                                           :contract-address pay-token-address})
       :unlimited-icon? false
       :label           pay-token-symbol
       :description     (address-utils/get-short-wallet-address pay-token-address)
       :avatar-props    {:token pay-token-symbol}}]]))

(defn- spender-contract-section
  []
  (let [theme            (quo.theme/use-theme)
        network          (rf/sub [:wallet/swap-network])
        provider         (rf/sub [:wallet/swap-proposal-provider])
        network-chain-id (:chain-id network)]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :spender-contract-label}
      (i18n/label :t/spender-contract)]
     [quo/approval-info
      {:type            :token-contract
       :option-icon     :i/options
       :on-option-press #(on-option-press {:chain-id         network-chain-id
                                           :contract-address (:contract-address provider)})
       :unlimited-icon? false
       :label           (:full-name provider)
       :description     (address-utils/get-short-wallet-address (:contract-address provider))
       :avatar-props    {:image (resources/get-network (:name provider))}}]]))

(defn- data-item
  [{:keys [network-image title subtitle size loading?]}]
  [quo/data-item
   {:container-style style/detail-item
    :blur?           false
    :card?           false
    :network-image   network-image
    :subtitle-type   (if network-image :network :default)
    :status          (if loading? :loading :default)
    :title           title
    :subtitle        subtitle
    :size            size}])

(defn- transaction-details
  []
  (let [network        (rf/sub [:wallet/swap-network])
        max-fees       (rf/sub [:wallet/wallet-swap-proposal-fee-fiat-formatted
                                constants/token-for-fees-symbol])
        loading-fees?  (rf/sub [:wallet/swap-loading-fees?])
        estimated-time (rf/sub [:wallet/swap-proposal-estimated-time])]
    [rn/view {:style style/details-container}
     [:<>
      [data-item
       {:title         (i18n/label :t/network)
        :subtitle      (:full-name network)
        :network-image (:source network)}]
      [data-item
       {:title    (i18n/label :t/est-time)
        :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time)})}]
      [data-item
       {:title    (i18n/label :t/max-fees)
        :subtitle max-fees
        :loading? loading-fees?
        :size     :small}]]]))

(defn- slide-button
  []
  (let [loading-fees?   (rf/sub [:wallet/swap-loading-fees?])
        account         (rf/sub [:wallet/current-viewing-account])
        on-auth-success (rn/use-callback #(rf/dispatch
                                           [:wallet/swap-transaction
                                            (security/safe-unmask-data %)]))]
    [standard-auth/slide-button
     {:size                :size-48
      :track-text          (i18n/label :t/slide-to-swap)
      :container-style     {:z-index 2}
      :customization-color (:color account)
      :disabled?           loading-fees?
      :on-auth-success     on-auth-success
      :auth-button-label   (i18n/label :t/confirm)}]))

(defn- footer
  []
  [rn/view {:style {:margin-bottom -10}}
   [transaction-details]
   [slide-button]])

(defn view
  []
  (let [account (rf/sub [:wallet/current-viewing-account])]
    [rn/view {:style style/container}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/close
                                   :on-press            events-helper/navigate-back
                                   :margin-top          8
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [footer]
       :gradient-cover?          true
       :customization-color      (:color account)}
      [:<>
       [swap-title]
       [spending-cap-section]
       [account-section]
       [token-section]
       [spender-contract-section]]]]))

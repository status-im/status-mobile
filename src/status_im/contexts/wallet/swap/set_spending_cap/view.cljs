(ns status-im.contexts.wallet.swap.set-spending-cap.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.common.utils.external-links :as external-links]
    [status-im.contexts.wallet.swap.set-spending-cap.style :as style]
    [utils.address :as address-utils]
    [utils.i18n :as i18n]
    [utils.navigation :as navigation]
    [utils.re-frame :as rf]))

(defn- swap-title
  [{:keys [pay-token-symbol pay-amount account provider]}]
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
      :customization-color (:color account)}]]])

(defn- spending-cap-section
  [{:keys [theme amount token-symbol]}]
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
     :label           (str amount " " token-symbol)
     :avatar-props    {:token token-symbol}}]])

(defn- account-section
  [{:keys [theme account pay-token-symbol pay-token-amount]}]
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
     :tag-label       (str pay-token-amount " " pay-token-symbol)
     :avatar-props    {:emoji               (:emoji account)
                       :customization-color (:color account)}}]])

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
  [{:keys [theme token-address token-symbol network-chain-id]}]
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
                                         :contract-address token-address})
     :unlimited-icon? false
     :label           token-symbol
     :description     (address-utils/get-short-wallet-address token-address)
     :avatar-props    {:token token-symbol}}]])

(defn- spender-contract-section
  [{:keys [theme provider network-chain-id]}]
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
     :avatar-props    {:image (resources/get-network (:name provider))}}]])

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
  [{:keys [estimated-time-min max-fees network loading-fees?]}]
  [rn/view {:style style/details-container}
   [:<>
    [data-item
     {:title         (i18n/label :t/network)
      :subtitle      (:full-name network)
      :network-image (:source network)}]
    [data-item
     {:title    (i18n/label :t/est-time)
      :subtitle (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
    [data-item
     {:title    (i18n/label :t/max-fees)
      :subtitle max-fees
      :loading? loading-fees?
      :size     :small}]]])

(defn footer
  [{:keys [estimated-time-min native-currency-symbol network theme account-color loading-fees?]}]
  (let [fee-formatted   (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol])
        on-auth-success (rn/use-callback #(js/alert "Not implemented yet"))]
    [rn/view {:style {:margin-bottom -10}}
     [transaction-details
      {:estimated-time-min estimated-time-min
       :max-fees           fee-formatted
       :network            network
       :loading-fees?      loading-fees?
       :theme              theme}]
     [standard-auth/slide-button
      {:size                :size-48
       :track-text          (i18n/label :t/slide-to-swap)
       :container-style     {:z-index 2}
       :customization-color account-color
       :disabled?           loading-fees?
       :on-auth-success     on-auth-success
       :auth-button-label   (i18n/label :t/confirm)}]]))

(defn view
  []
  (let [theme                   (quo.theme/use-theme)
        swap-transaction-data   (rf/sub [:wallet/swap])
        {:keys [asset-to-pay network pay-amount
                providers swap-proposal
                loading-fees?]} swap-transaction-data
        estimated-time-min      (:estimated-time swap-proposal)
        pay-token-symbol        (:symbol asset-to-pay)
        pay-token-address       (:address asset-to-pay)
        native-currency-symbol  (get-in swap-proposal [:from :native-currency-symbol])
        account                 (rf/sub [:wallet/current-viewing-account])
        account-color           (:color account)
        provider                (first providers)]
    [rn/view {:style style/container}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:icon-name           :i/close
                                   :on-press            navigation/navigate-back
                                   :margin-top          8
                                   :background          :blur
                                   :accessibility-label :top-bar}]
       :footer                   [footer
                                  {:estimated-time-min     estimated-time-min
                                   :native-currency-symbol native-currency-symbol
                                   :network                network
                                   :account-color          account-color
                                   :provider               provider
                                   :loading-fees?          loading-fees?
                                   :theme                  theme}]
       :gradient-cover?          true
       :customization-color      account-color}
      [:<>
       [swap-title
        {:pay-token-symbol pay-token-symbol
         :pay-amount       pay-amount
         :account          account
         :provider         provider}]
       [spending-cap-section
        {:token-symbol pay-token-symbol
         :amount       pay-amount
         :theme        theme}]
       [account-section
        {:account          account
         :pay-token-symbol pay-token-symbol
         :pay-token-amount pay-amount
         :theme            theme}]
       [token-section
        {:token-symbol     pay-token-symbol
         :token-address    pay-token-address
         :network-chain-id (:chain-id network)
         :theme            theme}]
       [spender-contract-section
        {:provider         provider
         :network-chain-id (:chain-id network)
         :theme            theme}]]]]))

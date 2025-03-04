(ns status-im.contexts.wallet.trading.swap.approval-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.common.utils.external-links :as external-links]
    [status-im.contexts.wallet.trading.swap.approval-confirmation.style :as style]
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
  (rf/dispatch [:dismiss-modal :screen/trading.swap-approval-confirmation])
  (rf/dispatch [:wallet.routes/sign-transactions password]))

(defn- swap-title
  []
  (let [pay-token-symbol (rf/sub [:trading.swap/pay-token-symbol])
        pay-amount       (rf/sub [:trading.swap/pay-amount])
        account          (rf/sub [:trading.swap/account])
        provider         (rf/sub [:trading.swap/provider])]
    [transaction.title/container
     [transaction.title/row {:first? true}
      [transaction.title/text
       {:label               (i18n/label :t/set-spending-cap-of)
        :accessibility-label :set-spending-cap-of}]]
     [transaction.title/row
      [quo/summary-tag
       {:token pay-token-symbol
        :label (str pay-amount " " pay-token-symbol)
        :type  :token}]
      [transaction.title/text
       {:label               (i18n/label :t/for)
        :accessibility-label :for}]]
     [transaction.title/row
      [quo/summary-tag
       {:label               (:full-name provider)
        :type                :network
        :image-source        (resources/get-network (:name provider))
        :customization-color (:color provider)}]
      [transaction.title/text
       {:label               (i18n/label :t/on)
        :accessibility-label :on}]]
     [transaction.title/row
      [quo/summary-tag
       {:label               (:name account)
        :type                :account
        :emoji               (:emoji account)
        :customization-color (:color account)}]]]))

(defn- spending-cap-section
  []
  (let [theme            (quo.theme/use-theme)
        pay-token-symbol (rf/sub [:trading.swap/pay-token-symbol])
        pay-amount       (rf/sub [:trading.swap/pay-amount])]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :spending-cap-label}
      (i18n/label :t/spending-cap)]
     (when (and pay-token-symbol pay-amount)
       [quo/approval-info
        {:type            :spending-cap
         :unlimited-icon? false
         :label           (str pay-amount " " pay-token-symbol)
         :avatar-props    {:token pay-token-symbol}}])]))

(defn- account-section
  []
  (let [theme            (quo.theme/use-theme)
        pay-token-symbol (rf/sub [:trading.swap/pay-token-symbol])
        account          (rf/sub [:trading.swap/account])
        pay-amount       (rf/sub [:trading.swap/pay-amount])]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :account-label}
      (i18n/label :t/account)]
     (when (and pay-token-symbol pay-amount)
       [quo/approval-info
        {:type            :account
         :unlimited-icon? false
         :label           (:name account)
         :description     (address-utils/get-short-wallet-address (:address account))
         :tag-label       (str pay-amount " " pay-token-symbol)
         :avatar-props    {:emoji               (:emoji account)
                           :customization-color (:color account)}}])]))

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
        pay-token         (rf/sub [:trading.swap/pay-token])
        network           (rf/sub [:trading.swap/network])
        pay-token-symbol  (:symbol pay-token)
        network-chain-id  (:chain-id network)
        pay-token-address (:address pay-token)]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :token-label}
      (i18n/label :t/token)]
     (when pay-token
       [quo/approval-info
        {:type            :token-contract
         :option-icon     :i/options
         :on-option-press #(on-option-press {:chain-id         network-chain-id
                                             :contract-address pay-token-address})
         :unlimited-icon? false
         :label           pay-token-symbol
         :description     (address-utils/get-short-wallet-address pay-token-address)
         :avatar-props    {:token pay-token-symbol}}])]))

(defn- spender-contract-section
  []
  (let [theme                    (quo.theme/use-theme)
        network                  (rf/sub [:trading.swap/network])
        provider                 (rf/sub [:trading.swap/provider])
        spender-contract-address (rf/sub [:trading.swap/approval-contract-address])
        network-chain-id         (:chain-id network)]
    [rn/view {:style style/summary-section-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :spender-contract-label}
      (i18n/label :t/spender-contract)]
     (when provider
       [quo/approval-info
        {:type            :token-contract
         :option-icon     :i/options
         :on-option-press #(on-option-press {:chain-id         network-chain-id
                                             :contract-address spender-contract-address})
         :unlimited-icon? false
         :label           (:full-name provider)
         :description     (address-utils/get-short-wallet-address spender-contract-address)
         :avatar-props    {:image (resources/get-network (:name provider))}}])]))

(defn- transaction-info-bar
  []
  (let [approval-fees  (rf/sub [:trading.swap/approval-fee-fiat])
        estimated-time (rf/sub [:trading.swap/approval-estimated-time])]
    [info-bar/container {:style {:padding-top 7 :padding-horizontal 4}}
     [info-bar/network]
     [info-bar/fiat-max-fee {:fee-amount approval-fees}]
     [info-bar/estimated-time {:estimated-time-mins estimated-time}]]))

(defn- slide-button
  []
  (let [swap-proposal           (rf/sub [:trading.swap/route])
        customization-color     (rf/sub [:trading.swap/customization-color])
        transaction-for-signing (rf/sub [:wallet.routes/transactions-for-signing])
        sign-on-keycard?        (get-in transaction-for-signing
                                        [:signingDetails :signOnKeycard])]
    [standard-auth/slide-button
     {:size                :size-48
      :track-text          (i18n/label :t/slide-to-sign)
      :container-style     {:z-index 2}
      :customization-color customization-color
      :disabled?           (not swap-proposal)
      :on-complete         (when sign-on-keycard? #(on-confirm ""))
      :on-auth-success     on-confirm
      :auth-button-label   (i18n/label :t/confirm)}]))

(defn- footer
  []
  [:<>
   [transaction-info-bar]
   [slide-button]
   [transaction.provider-footer/view]])

(defn view
  []
  [transaction.container/view {:footer footer}
   [swap-title]
   [spending-cap-section]
   [account-section]
   [token-section]
   [spender-contract-section]])

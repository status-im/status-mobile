(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [legacy.status-im.utils.utils :as utils]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- transaction-title
  [{:keys [token-symbol amount account to-address]}]
  [rn/view {:style style/content-container}
   [rn/view {:style {:flex-direction :row}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/send)]
    [quo/summary-tag
     {:token        token-symbol
      :label        (str amount " " token-symbol)
      :type         :token
      :image-source :eth}]]
   [rn/view
    {:style {:flex-direction :row
             :margin-top     4}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/from)]
    [quo/summary-tag
     {:label               (:name account)
      :type                :account
      :emoji               (:emoji account)
      :customization-color (:color account)}]]
   [rn/view
    {:style {:flex-direction :row
             :margin-top     4}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/to)]
    [quo/summary-tag
     {:type  :address
      :label (utils/get-shortened-address to-address)}]]])

(defn- user-summary
  [{:keys [amount account-props theme label accessibility-label summary-type]}]
  [rn/view
   {:style {:padding-horizontal 20
            :padding-bottom     16}}
   [quo/text
    {:size                :paragraph-2
     :weight              :medium
     :style               (style/section-label theme)
     :accessibility-label accessibility-label}
    label]
   [quo/summary-info
    {:type          summary-type
     :networks?     true
     :values        {:ethereum amount}
     :account-props account-props}]])

(defn- transaction-details
  [{:keys [estimated-time-min max-fees token amount to-address theme]}]
  (let [currency-symbol (rf/sub [:profile/currency-symbol])]
    [rn/view
     {:style style/details-title-container}
     [quo/text
      {:size                :paragraph-2
       :weight              :medium
       :style               (style/section-label theme)
       :accessibility-label :summary-from-label}
      (i18n/label :t/details)]
     [rn/view
      {:style (style/details-container theme)}
      [quo/data-item
       {:container-style style/detail-item
        :blur?           false
        :description     :default
        :icon-right?     false
        :card?           false
        :label           :none
        :status          :default
        :size            :small
        :title           (i18n/label :t/est-time)
        :subtitle        (i18n/label :t/time-in-mins {:minutes (str estimated-time-min)})}]
      [quo/data-item
       {:container-style style/detail-item
        :blur?           false
        :description     :default
        :icon-right?     false
        :card?           false
        :label           :none
        :status          :default
        :size            :small
        :title           (i18n/label :t/max-fees)
        :subtitle        (i18n/label :t/amount-with-currency-symbol
                                     {:amount (str max-fees)
                                      :symbol currency-symbol})}]
      [quo/data-item
       {:container-style style/detail-item
        :blur?           false
        :description     :default
        :icon-right?     false
        :card?           false
        :label           :none
        :status          :default
        :size            :small
        :title           (i18n/label :t/user-gets {:name (utils/get-shortened-address to-address)})
        :subtitle        (str amount " " (:symbol token))}]]]))

(defn- view-internal
  [_]
  (let [on-close              #(rf/dispatch [:navigate-back-within-stack :wallet-select-asset])
        send-transaction-data (rf/sub [:wallet/wallet-send])
        token                 (:token send-transaction-data)
        token-symbol          (:symbol token)
        amount                (:amount send-transaction-data)
        route                 (:route send-transaction-data)
        estimated-time-min    (:estimated-time route)
        max-fees              "-"
        to-address            (:to-address send-transaction-data)
        account               (rf/sub [:wallet/current-viewing-account])
        account-color         (:color account)
        from-account-props    {:customization-color account-color
                               :size                32
                               :emoji               (:emoji account)
                               :type                :default
                               :name                (:name account)
                               :address             (utils/get-shortened-address (:address account))}
        user-props            {:full-name to-address
                               :address   (utils/get-shortened-address to-address)}]
    (prn route)

    (fn [{:keys [theme]}]
      [rn/view {:style {:flex 1}}
       [floating-button-page/view
        {:header              [quo/page-nav
                               {:icon-name           :i/arrow-left
                                :on-press            on-close
                                :margin-top          (safe-area/get-top)
                                :background          :blur
                                :accessibility-label :top-bar
                                :right-side          [{:icon-name           :i/advanced
                                                       :on-press            #(js/alert
                                                                              "to be implemented")
                                                       :accessibility-label :advanced-options}]}]
         :footer              [standard-auth/slide-button
                               {:size                :size-48
                                :track-text          (i18n/label :t/slide-to-send)
                                :container-style     {:z-index 2}
                                :customization-color account-color
                                :on-auth-success     #(rf/dispatch [:wallet/send-transaction %])
                                :auth-button-label   (i18n/label :t/confirm)}]
         :gradient-cover?     true
         :customization-color (:color account)}
        [rn/view
         [transaction-title
          {:token-symbol token-symbol
           :amount       amount
           :account      account
           :to-address   to-address}]
         [user-summary
          {:amount              amount
           :summary-type        :status-account
           :accessibility-label :summary-from-label
           :label               (i18n/label :t/from-capitalized)
           :account-props       from-account-props
           :theme               theme}]
         [user-summary
          {:amount              amount
           :summary-type        :account
           :accessibility-label :summary-to-label
           :label               (i18n/label :t/to-capitalized)
           :account-props       user-props
           :theme               theme}]
         [transaction-details
          {:estimated-time-min estimated-time-min
           :max-fees           max-fees
           :token              token
           :amount             amount
           :to-address         to-address
           :theme              theme}]]]])))

(def view (quo.theme/with-theme view-internal))

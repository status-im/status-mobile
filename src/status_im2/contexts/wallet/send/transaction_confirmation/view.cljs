(ns status-im2.contexts.wallet.send.transaction-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.utils.utils :as utils]
    [status-im2.common.resources :as resources]
    [status-im2.common.standard-authentication.core :as standard-auth]
    [status-im2.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- transaction-title
  [token amount account to-address]
  [rn/view {:style style/content-container}
   [rn/view {:style {:flex-direction :row}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/send)]
    [quo/summary-tag
     {:label        (str amount " " (:symbol token))
      :type         :token
      :image-source (quo.resources/get-token :eth)}]]
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

(defn- transaction-from
  [amount account-props theme]
  [rn/view
   {:style {:padding-horizontal 20
            :padding-bottom     16}}
   [quo/text
    {:size                :paragraph-2
     :weight              :medium
     :style               (style/section-label theme)
     :accessibility-label :summary-from-label}
    (i18n/label :t/from-capitalized)]
   [quo/summary-info
    {:type          :status-account
     :networks?     true
     :values        {:ethereum amount
                     :optimism 0
                     :arbitrum 0}
     :account-props account-props}]])

(defn- transaction-to
  [amount user-props theme]
  [rn/view
   {:style {:padding-horizontal 20
            :padding-bottom     16}}
   [quo/text
    {:size                :paragraph-2
     :weight              :medium
     :style               (style/section-label theme)
     :accessibility-label :summary-from-label}
    (i18n/label :t/to-capitalized)]
   [quo/summary-info
    {:type          :user
     :networks?     true
     :values        {:ethereum amount
                     :optimism 0
                     :arbitrum 0}
     :account-props user-props}]])

(defn- transaction-details
  [token amount to-address theme]
  [rn/view
   {:style {:padding-horizontal 20
            :padding-bottom     16}}
   [quo/text
    {:size                :paragraph-2
     :weight              :medium
     :style               (style/section-label theme)
     :accessibility-label :summary-from-label}
    (i18n/label :t/details)]
   [rn/view
    {:style (style/details-container theme)}
    [quo/data-item
     {:container-style {:flex   1
                        :height 36}
      :blur?           false
      :description     :default
      :icon-right?     false
      :card?           false
      :label           :none
      :status          :default
      :size            :small
      :title           (i18n/label :t/est-time)
      :subtitle        "3-5 min"}]
    [quo/data-item
     {:container-style {:flex   1
                        :height 36}
      :blur?           false
      :description     :default
      :icon-right?     false
      :card?           false
      :label           :none
      :status          :default
      :size            :small
      :title           (i18n/label :t/max-fees)
      :subtitle        "€188,70"}]
    [quo/data-item
     {:container-style {:flex   1
                        :height 36}
      :blur?           false
      :description     :default
      :icon-right?     false
      :card?           false
      :label           :none
      :status          :default
      :size            :small
      :title           (i18n/label :t/user-gets {:name (utils/get-shortened-address to-address)})
      :subtitle        (str amount " " (:symbol token))}]]])

(defn- f-view-internal
  [_]
  (let [margin-top            (safe-area/get-top)
        biometric-auth?       false
        on-close              #(rf/dispatch [:navigate-back-within-stack :wallet-select-asset])
        send-transaction-data (get-in (rf/sub [:wallet]) [:ui :send])
        token                 (:token send-transaction-data)
        amount                (:amount send-transaction-data)
        routes                (:routes send-transaction-data)
        to-address            (:to-address send-transaction-data)
        account               (rf/sub [:wallet/current-viewing-account])
        account-props         {:customization-color (:color account)
                               :size                32
                               :emoji               (:emoji account)
                               :type                :default
                               :name                (:name account)
                               :address             (utils/get-shortened-address (:address account))}
        user-props            {:full-name           "M L"
                               :status-indicator?   false
                               :size                :small
                               :ring-background     (resources/get-mock-image :ring)
                               :customization-color :blue
                               :name                "Mark Libot"
                               :address             "0x0ah...78b"
                               :status-account      (merge account-props
                                                           {:size  16
                                                            :name  "New house"
                                                            :emoji "🍔"})}]
    (fn [{:keys [theme]}]
      [rn/view {:style {:flex 1}}
       [quo/gradient-cover {:customization-color (:color account)}]
       [rn/view {:style (style/container margin-top)}
        [quo/page-nav
         {:icon-name           :i/arrow-left
          :on-press            on-close
          :accessibility-label :top-bar
          :right-side          [{:icon-name           :i/advanced
                                 :on-press            (fn callback [] nil)
                                 :accessibility-label "Advanced"}]}]
        [transaction-title token amount account to-address]
        [transaction-from amount account-props theme]
        [transaction-to amount user-props theme]
        [transaction-details token amount to-address theme]
        [rn/view {:style style/slide-button-container}
         [standard-auth/slide-button
          {:size                :size-40
           :track-text          (i18n/label :t/slide-to-send)
           :customization-color (:color account)
           :on-enter-password   #(rf/dispatch [:wallet/send-transaction %])
           :biometric-auth?     biometric-auth?
           :auth-button-label   (i18n/label :t/confirm)}]]]])))

(defn view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))

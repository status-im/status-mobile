(ns status-im.contexts.wallet.send.transaction-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- transaction-title
  []
  [rn/view {:style style/content-container}
   [rn/view {:style {:flex-direction :row}}
    [quo/text
     {:size                :heading-1
      :weight              :semi-bold
      :style               style/title-container
      :accessibility-label :send-label}
     (i18n/label :t/send)]
    [quo/summary-tag
     {:label        "150 ETH"
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
     {:label               "Collectibles vault"
      :type                :account
      :emoji               "🍑"
      :customization-color :purple}]]
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
     {:label               "Mark Libot"
      :type                :user
      :image-source        (resources/get-mock-image :user-picture-male4)
      :customization-color :magenta}]]])

(defn- transaction-from
  [status-account-props theme]
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
     :values        {:ethereum 150
                     :optimism 50
                     :arbitrum 25}
     :account-props status-account-props}]])

(defn- transaction-to
  [user-props theme]
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
     :values        {:ethereum 150
                     :optimism 50
                     :arbitrum 25}
     :account-props user-props}]])

(defn- transaction-details
  [theme]
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
      :title           (i18n/label :t/user-gets {:name "Mark"})
      :subtitle        "149.99 ETH"}]]])

(defn- f-view-internal
  [_]
  (let [reset-slider?        (reagent/atom false)
        margin-top           (safe-area/get-top)
        biometric-auth?      true
        on-close             #(rf/dispatch [:navigate-back-within-stack :wallet-select-asset])
        status-account-props {:customization-color :purple
                              :size                32
                              :emoji               "🍑"
                              :type                :default
                              :name                "Collectibles vault"
                              :address             "0x0ah...78b"}
        user-props           {:full-name           "M L"
                              :status-indicator?   false
                              :size                :small
                              :ring-background     (resources/get-mock-image :ring)
                              :customization-color :blue
                              :name                "Mark Libot"
                              :address             "0x0ah...78b"
                              :status-account      (merge status-account-props
                                                          {:size  16
                                                           :name  "New house"
                                                           :emoji "🍔"})}]
    (fn [{:keys [theme]}]
      [rn/view {:style {:flex 1}}
       [quo/gradient-cover {:customization-color :purple}]
       [rn/view {:style (style/container margin-top)}
        [quo/page-nav
         {:icon-name           :i/arrow-left
          :on-press            on-close
          :accessibility-label :top-bar
          :right-side          [{:icon-name           :i/advanced
                                 :on-press            (fn callback [] nil)
                                 :accessibility-label "Advanced"}]}]
        [transaction-title]
        [transaction-from status-account-props theme]
        [transaction-to user-props theme]
        [transaction-details theme]
        [rn/view {:style style/slide-button-container}
         [quo/slide-button
          {:size                :size/s-48
           :customization-color :purple
           :on-reset            (when @reset-slider? #(reset! reset-slider? false))
           :on-complete         #(js/alert "Not implemented yet")
           :track-icon          (if biometric-auth? :i/face-id :password)
           :track-text          (i18n/label :t/slide-to-send)}]]]])))

(defn view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))

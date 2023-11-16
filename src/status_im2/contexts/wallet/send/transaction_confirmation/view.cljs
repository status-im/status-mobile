(ns status-im2.contexts.wallet.send.transaction-confirmation.view
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.wallet.send.transaction-confirmation.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- f-view-internal
  [theme]
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
    (fn []
      [rn/view {:style {:flex 1}}
       [quo/gradient-cover
        {:customization-color :purple}]
       [rn/view
        {:style (style/container margin-top)}
        [quo/page-nav
         {:icon-name           :i/arrow-left
          :on-press            on-close
          :accessibility-label :top-bar
          :right-side          {:icon-name           :i/advanced
                                :on-press            (fn callback [] nil)
                                :accessibility-label "Advanced"}
          :account-switcher    {:customization-color :purple
                                :on-press            #(js/alert "Not implemented yet")
                                :state               :default
                                :emoji               "🍑"}}]
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
            :customization-color :magenta}]]]
        [rn/view
         {:style {:padding-horizontal 20
                  :padding-bottom     16}}
         [quo/text
          {:size                :paragraph-2
           :weight              :medium
           :style               {:margin-bottom 8}
           :accessibility-label :summary-from-label}
          (i18n/label :t/from-capitalized)]
         [quo/summary-info
          {:type          :status-account
           :networks?     true
           :values        {:ethereum 150
                           :optimism 50
                           :arbitrum 25}
           :account-props status-account-props}]]
        [rn/view
         {:style {:padding-horizontal 20
                  :padding-bottom     16}}
         [quo/text
          {:size                :paragraph-2
           :weight              :medium
           :style               {:margin-bottom 8}
           :accessibility-label :summary-from-label}
          (i18n/label :t/to-capitalized)]
         [quo/summary-info
          {:type          :user
           :networks?     true
           :values        {:ethereum 150
                           :optimism 50
                           :arbitrum 25}
           :account-props user-props}]]
        [rn/view
         {:style {:padding-horizontal 20
                  :padding-bottom     16}}
         [quo/text
          {:size                :paragraph-2
           :weight              :medium
           :style               {:margin-bottom 8}
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
            :title           "Est. time"
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
            :title           "Max fees"
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
            :title           "Mark gets"
            :subtitle        "149.99 ETH"}]]]
        [rn/safe-area-view
         {:style style/slide-button-container}
         [quo/slide-button
          {:size                :size/s-48
           :customization-color :purple
           :on-reset            (when @reset-slider? #(reset! reset-slider? false))
           :on-complete         #(js/alert "Not implemented yet")
           :track-icon          (if biometric-auth? :i/face-id :password)
           :track-text          "Slide to send"}]]]])))

(defn- view-internal
  [{:keys [theme]}]
  [:f> f-view-internal theme])

(def view (quo.theme/with-theme view-internal))

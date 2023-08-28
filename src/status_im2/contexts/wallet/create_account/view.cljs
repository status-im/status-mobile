(ns status-im2.contexts.wallet.create-account.view
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.core :as quo]
    [re-frame.core :as rf]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]
    [status-im2.contexts.wallet.create-account.style :as style]))

(defn- view-internal
  [{:keys [theme]}]
  (let [top    (safe-area/get-top)
        bottom (safe-area/get-bottom)]
    [rn/view
     {:style {:flex       1
              :margin-top top}}
     [quo/page-nav
      {:align-mid?            true
       :mid-section           {:type :text-only :main-text ""}
       :left-section          {:type            :grey
                               :icon            :i/close
                               :icon-background :photo
                               :on-press        #(rf/dispatch [:navigate-back])}
       :right-section-buttons [{:type            :grey
                                :icon            :i/info
                                :icon-background :photo
                                :on-press        #(rf/dispatch [:open-modal :how-to-pair])}]}]
     [quo/gradient-cover
      {:customization-color :blue
       :container-style     (style/gradient-cover-container top)}]
     [rn/view
      {:style {:padding-horizontal 20
               :padding-top        12}}
      [quo/account-avatar
       {:customization-color :blue
        :size                80
        :emoji               "\uD83D\uDC8E"
        :type                :default}]
      ;; TODO: check and fix button blur
      [quo/button
       {:size            32
        :type            :grey
        :background      :photo
        :icon-only?      true
        :on-press        #(js/alert "pressed")
        :container-style style/reaction-button-container} :i/reaction]]
     ;; TODO: title-input needs to have icon
     [quo/title-input
      {:color           :red
       :placeholder     "Type something here"
       :max-length      24
       :blur?           true
       :disabled?       false
       :default-value   "Account 2"
       :container-style style/title-input-container}]
     [rn/view
      {:style {:padding-vertical   12
               :padding-horizontal 20}}
      ;; TODO: implement label component
      [quo/text
       {:size   :paragraph-2
        :weight :medium
        :style  {:color          (colors/theme-colors colors/neutral-50 colors/neutral-40)
                 :padding-bottom 4}} (i18n/label :t/colour)]
      ;; TODO: update color picker component
      [quo/color-picker {:selected :orange}]]
     ;; TODO: implement divider-line component
     [rn/view {:style (style/divider-line theme)}]
     ;; TODO: update settings-list component to accept avatar, and subtitle,
     ;; text inside button not vertically centered, and should be able to accept button with icon
     [quo/category
      {:list-type :settings
       :label     (i18n/label :t/origin)
       :data      temp/create-account-state}]
     [quo/slide-button
      {:track-text          "We gotta slide"
       :track-icon          :face-id
       :customization-color :blue
       :on-complete         (fn []
                              (js/alert "I don't wanna slide anymore"))
       :container-style     (style/slide-button-container bottom)}]]))

(def view (quo.theme/with-theme view-internal))

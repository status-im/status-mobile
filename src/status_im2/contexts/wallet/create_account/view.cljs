(ns status-im2.contexts.wallet.create-account.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [quo2.core :as quo]
    [re-frame.core :as rf]
    [react-native.safe-area :as safe-area]
    [utils.i18n :as i18n]))

(defn view
  []
  (let [top (safe-area/get-top)]
    [rn/view
     {:style {:flex       1
              :margin-top top}}
     [quo/page-nav
      {
       :align-mid?            true
       :mid-section           {:type :text-only :main-text ""}
       :left-section          {:type     :grey
                               :icon     :i/close
                               :on-press #(rf/dispatch [:navigate-back])}
       :right-section-buttons [{:type     :grey
                                :icon     :i/info
                                :on-press #(rf/dispatch [:open-modal :how-to-pair])}]}]
     [quo/gradient-cover {:customization-color :blue
                          :container-style     {:position :absolute
                                                :top      (- top)
                                                :left     0
                                                :right    0}}]
     [rn/view {:style {:padding-horizontal 20
                       :padding-top        12}}
      [quo/account-avatar {:customization-color :blue
                           :size                80
                           :emoji               "\uD83D\uDC8E"
                           :type                :default}]
      ;; TODO: button need to be blur
      [quo/button {:size            32
                   :type            :grey
                   :background :photo
                   :icon-only?      true
                   :on-press        #(js/alert "pressed")
                   :container-style {:position :absolute
                                     :bottom   0
                                     :left     80}} :i/reaction]]
     ;; TODO: title-input needs to have icon
     [quo/title-input {:color       :red
                       :placeholder "Type something here"
                       :max-length  24
                       :blur?       true
                       :disabled?   false
                       :default-value "Account 2"
                       :container-style {:padding-horizontal 20
                                         :padding-top 12
                                         :padding-bottom 16}}]
     [rn/view {:style {:padding-vertical 12
                       :padding-horizontal 20}}
      [quo/text {:size   :paragraph-2
                 :weight :medium
                 :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
                          :padding-bottom 4}} (i18n/label :t/colour)]
      [quo/color-picker {:selected :orange}]]
     ;; TODO: implement divider-line component
     [rn/view {:style {:border-color (colors/theme-colors colors/neutral-10 colors/neutral-90)
                       :padding-top 12
                       :padding-bottom 8
                       :border-bottom-width 1}}]
     ]))

(ns status-im.contexts.profile.settings.screens.password.change-password.confirmation-sheet
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  [:<>
   [quo/drawer-top
    {:type  :default
     :blur? true
     :title (i18n/label :t/re-encrypt-data)}]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :paragraph-1
      :style  {:margin-top    4
               :margin-bottom 8}
      :weight :regular}
     (i18n/label :t/change-password-confirm-description)]
    [quo/information-box
     {:type  :error
      :style {:margin-top    4
              :margin-bottom 12}
      :icon  :i/info}
     (i18n/label :t/change-password-confirm-warning)]]
   [quo/bottom-actions
    {:actions          :two-actions
     :button-two-label (i18n/label :t/cancel)
     :button-two-props {:type       :grey
                        :background :blur
                        :on-press   (fn [] (rf/dispatch [:hide-bottom-sheet]))}
     :button-one-label (i18n/label :t/re-encrypt)
     :button-one-props {:type                :danger
                        :customization-color :sky
                        :on-press            (fn []
                                               (rf/dispatch
                                                [:password-settings/change-password-submit]))}}]])

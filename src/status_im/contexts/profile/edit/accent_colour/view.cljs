(ns status-im.contexts.profile.edit.accent-colour.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.contexts.profile.edit.accent-colour.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn f-view
  [{:keys [customization-color]}]
  (let [{window-width :width} (rn/get-window)
        unsaved-custom-color  (reagent/atom customization-color constants/profile-default-color)
        on-change             #(reset! unsaved-custom-color %)]
    (fn [{:keys [customization-color]}]
      (let [profile         (rf/sub [:profile/profile-with-image
                                     {:customization-color @unsaved-custom-color}])
            profile-picture (profile.utils/photo profile)
            display-name    (profile.utils/displayed-name profile)]
        (rn/use-effect
         #(on-change customization-color)
         [customization-color])
        [quo/overlay
         {:type            :shell
          :container-style (style/page-wrapper safe-area/insets)}
         [quo/page-nav
          {:key        :header
           :background :blur
           :icon-name  :i/arrow-left
           :on-press   #(rf/dispatch [:navigate-back])}]
         [rn/view
          {:key   :content
           :style style/screen-container}
          [rn/view {:style {:flex 1}}
           [rn/view {:style style/padding-horizontal}
            [quo/text-combinations {:title (i18n/label :t/accent-colour)}]
            [quo/profile-card
             {:profile-picture     profile-picture
              :name                display-name
              :card-style          style/profile-card
              :customization-color @unsaved-custom-color}]
            [quo/text
             {:size   :paragraph-2
              :weight :medium
              :style  style/color-title}
             (i18n/label :t/accent-colour)]]
           [quo/color-picker
            {:blur?            true
             :default-selected @unsaved-custom-color
             :on-change        on-change
             :container-style  style/padding-horizontal
             :window-width     window-width}]]
          [rn/view {:style style/button-wrapper}
           [quo/button
            {:type                :primary
             :customization-color @unsaved-custom-color
             :on-press            (fn []
                                    (rf/dispatch [:profile/edit-accent-colour
                                                  {:color @unsaved-custom-color}]))}
            (i18n/label :t/save-colour)]]]]))))

(defn view
  []
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:f> f-view {:customization-color customization-color}]))

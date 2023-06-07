(ns quo2.components.inputs.profile-input.view
  (:require
    [quo2.components.buttons.button :as buttons]
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [quo2.components.inputs.profile-input.style :as style]
    [quo2.components.inputs.title-input.view :as title-input]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.hole-view :as hole-view]))

(defn profile-input
  [{:keys [customization-color
           placeholder
           on-press
           title-input-props
           image-picker-props]}]
  (let [full-name (:full-name image-picker-props)]
    [rn/view
     {:style (style/container customization-color)}
     [rn/view
      (if platform/ios?
        [hole-view/hole-view
         {:holes [{:x            33
                   :y            24
                   :width        24
                   :height       24
                   :borderRadius 12}]}
         [user-avatar/user-avatar
          (assoc image-picker-props
                 :customization-color customization-color
                 :full-name           (if (seq full-name)
                                        full-name
                                        placeholder)
                 :status-indicator?   false
                 :size                :medium)]]
        [user-avatar/user-avatar
         (assoc image-picker-props
                :customization-color customization-color
                :full-name           (if (seq full-name)
                                       full-name
                                       placeholder)
                :status-indicator?   false
                :size                :medium)])
      [buttons/button
       {:accessibility-label       :select-profile-picture-button
        :type                      :grey
        :override-theme            :dark
        :override-background-color (colors/alpha colors/white 0.05)
        :on-press                  on-press
        :icon-size                 20
        :width                     24
        :size                      24
        :icon                      :i/camera
        :style                     style/button
        :inner-style               style/button-inner} :i/camera]]
     [rn/view {:style style/input-container}
      [title-input/title-input
       (merge title-input-props
              {:blur?               true
               :override-theme      :dark
               :placeholder         placeholder
               :customization-color customization-color})]]]))

(ns quo2.components.inputs.profile-input.view
  (:require
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [quo2.components.buttons.button.view :as buttons]
    [quo2.components.inputs.profile-input.style :as style]
    [quo2.components.inputs.title-input.view :as title-input]
    [react-native.core :as rn]
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
      [hole-view/hole-view
       ;; Force re-render hole view when props are changed
       ;; https://github.com/status-im/status-mobile/issues/15577
       {:key   (hash
                (if (:profile-picture image-picker-props)
                  (:profile-picture image-picker-props)
                  image-picker-props))
        :holes [{:x            33
                 :y            24
                 :width        24
                 :height       24
                 :borderRadius 12}]}
       [user-avatar/user-avatar
        (assoc image-picker-props
               :customization-color customization-color
               :static?             true
               :status-indicator?   false
               :full-name           (if (seq full-name) full-name placeholder)
               :size                :medium)]]
      [buttons/button
       {:accessibility-label :select-profile-picture-button
        :type                :grey
        :background          :blur
        :on-press            on-press
        :size                24
        :icon-only?          true
        :container-style     style/button
        :inner-style         style/button-inner} :i/camera]]
     [rn/view {:style style/input-container}
      [title-input/view
       (merge title-input-props
              {:blur?               true
               :placeholder         placeholder
               :customization-color customization-color})]]]))

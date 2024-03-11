(ns quo.components.profile.select-profile.view
  (:require
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.profile.select-profile.style :as style]
    [react-native.core :as rn]))

(defn view
  "Options
   - `selected?` Selected state. Use when the component is controlled.
   - `name` Full name
   - `profile-picture` Profile picture
   - `customization-color` Customization color
   - `(on-change selected?)` On change event handler
   "
  [{:keys [selected? profile-picture name customization-color on-change]
    :or   {customization-color :turquoise}}]
  [rn/touchable-opacity
   {:style               (style/container customization-color selected?)
    :on-press            #(when on-change (on-change (not selected?)))
    :accessibility-label :select-profile}
   [rn/view {:style style/header}
    [user-avatar/user-avatar
     {:full-name         name
      :status-indicator? false
      :size              :medium
      :profile-picture   profile-picture}]
    [rn/view {:style (style/select-radio selected?)}
     (when selected?
       [rn/view {:style style/select-radio-inner}])]]
   [text/text
    {:size   :heading-2
     :weight :semi-bold
     :style  style/profile-name}
    name]])

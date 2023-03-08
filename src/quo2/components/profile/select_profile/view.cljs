(ns quo2.components.profile.select-profile.view
  (:require
    [quo2.components.profile.select-profile.style :as style]
    [react-native.core :as rn]
    [quo2.components.markdown.text :as text]
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [reagent.core :as reagent]))

(defn- on-change-handler
  [selected? on-change]
  (swap! selected? not)
  (when on-change (on-change @selected?)))

(defn view
  "Options
   - `default-selected?` The default selected state. Use when the component is not controlled.
   - `selected?` Selected state. Use when the component is controlled.
   - `name` Full name
   - `profile-picture` Profile picture
   - `customization-color` Customization color
   - `(on-change selected?)` On change event handler
   "
  [{:keys [default-selected?]}]
  (let [internal-selected? (reagent/atom (or default-selected? false))]
    (fn [{:keys [selected?
                 profile-picture
                 ring-background
                 name
                 customization-color
                 on-change]
          :or   {customization-color :turquoise}}]
      (let [avatar-image-key (if profile-picture :profile-picture :ring-background)
            picture          (or profile-picture ring-background)]
        (when (and (not (nil? selected?)) (not= @internal-selected? selected?))
          (reset! internal-selected? selected?))
        [rn/touchable-opacity
         {:style               (style/container customization-color @internal-selected?)
          :on-press            #(on-change-handler internal-selected? on-change)
          :active-opacity      1
          :accessibility-label :select-profile}
         [rn/view {:style style/header}
          [user-avatar/user-avatar
           {:full-name         name
            :status-indicator? false
            :size              :medium
            avatar-image-key   picture}]
          [rn/view {:style (style/select-radio @internal-selected?)}
           (when @internal-selected? [rn/view {:style style/select-radio-inner}])]]
         [text/text
          {:size   :heading-2
           :weight :semi-bold
           :style  style/profile-name}
          name]]))))


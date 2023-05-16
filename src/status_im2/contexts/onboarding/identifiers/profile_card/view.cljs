(ns status-im2.contexts.onboarding.identifiers.profile-card.view
  (:require [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.reanimated :as reanimated]
            [react-native.fast-image :as fast-image]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.profile.profile-card.style :as profile-card-style]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [utils.worklets.identifiers-highlighting :as worklets.identifiers-highlighting]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.onboarding.identifiers.profile-card.style :as style]))

(defn- f-profile-card-component
  [{:keys [profile-picture name hash emoji-hash on-card-press
           customization-color card-style progress]
    :or   {customization-color  :turquoise
           card-style           {:padding-horizontal 20
                                 :flex               1}}}]
  (let [background-style  (worklets.identifiers-highlighting/background-style 
                           (colors/custom-color customization-color 50) @progress)
        opacity           (worklets.identifiers-highlighting/opacity @progress)
        ring-style        (worklets.identifiers-highlighting/ring-style @progress)
        user-hash-style   (worklets.identifiers-highlighting/user-hash-style colors/white @progress)
        emoji-hash-style  (worklets.identifiers-highlighting/emoji-hash-style @progress)
        identicon-ring    (resources/get-image :identicon-ring)] 
    [rn/touchable-without-feedback
     {:on-press            on-card-press
      :accessibility-label :profile-card}
     [hole-view/hole-view
      {:key   (str name) ;; Key is required to force removal of holes
       :style (merge {:flex-direction :row} card-style)
       :holes []}
      [reanimated/view (style/card-container background-style)
       [rn/view {:style profile-card-style/card-header}
        [reanimated/view (style/opacity opacity {})
         [user-avatar/user-avatar
          {:full-name           name
           :profile-picture     profile-picture
           :ring-background     identicon-ring
           :override-theme      :dark
           :size                :medium
           :status-indicator?   false
           :customization-color customization-color}]]
        [reanimated/view (style/identicon-ring ring-style)
         [fast-image/fast-image
          {:accessibility-label :ring-background
           :style               (style/identicon-ring-image)
           :source              identicon-ring}]]]
       [reanimated/view (style/opacity opacity profile-card-style/name-container)
        [text/text
         {:size            :heading-2
          :weight          :semi-bold
          :number-of-lines 1
          :style           profile-card-style/user-name} name]] 
       [reanimated/view 
        {:style [user-hash-style]}
        [text/text
         {:weight          :monospace
          :number-of-lines 1
          :style           profile-card-style/user-hash} hash]]
       (when emoji-hash
         [reanimated/view
          {:style [emoji-hash-style]}
          [text/text
           {:weight          :monospace
            :number-of-lines 1
            :style           profile-card-style/emoji-hash} emoji-hash]])]]]))

(defn profile-card
  [props]
  [:f> f-profile-card-component props])

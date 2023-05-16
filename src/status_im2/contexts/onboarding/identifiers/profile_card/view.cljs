(ns status-im2.contexts.onboarding.identifiers.profile-card.view
  (:require [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.reanimated :as reanimated]
            [react-native.masked-view :as masked-view]
            [react-native.fast-image :as fast-image]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [utils.worklets.identifiers-highlighting :as worklets.identifiers-highlighting]
            [status-im2.contexts.onboarding.identifiers.profile-card.style :as style]))

(defn- f-profile-card-component
  [{:keys [profile-picture name hash emoji-hash
           customization-color progress]
    :or   {customization-color :turquoise}}]
  (let [container-background (worklets.identifiers-highlighting/background
                              (colors/custom-color customization-color 50)
                              @progress)
        opacity              (worklets.identifiers-highlighting/opacity @progress)
        avatar-opacity       (worklets.identifiers-highlighting/avatar-opacity @progress)
        ring-opacity         (worklets.identifiers-highlighting/ring-opacity @progress)
        user-hash-color      (worklets.identifiers-highlighting/user-hash-color @progress)
        user-hash-opacity    (worklets.identifiers-highlighting/user-hash-opacity @progress)
        emoji-hash-style     (worklets.identifiers-highlighting/emoji-hash-style @progress)]
    [rn/touchable-without-feedback
     {:accessibility-label :profile-card}
     [hole-view/hole-view
      {:key   (str name)
       :style style/card-view
       :holes []}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:background-color container-background}
                style/card-container)}
       ;; user avatar with mask view
       [rn/view {:style style/card-header}
        [reanimated/view
         {:style (reanimated/apply-animations-to-style
                  {:opacity avatar-opacity}
                  {})}
         [quo/user-avatar
          {:full-name           name
           :profile-picture     profile-picture
           :override-theme      :dark
           :size                :medium
           :status-indicator?   false
           :customization-color customization-color}]]
        [reanimated/view
         {:style (reanimated/apply-animations-to-style
                  {:opacity ring-opacity}
                  style/mask-container)}
         [masked-view/masked-view
          {:mask-element (reagent/as-element
                          [rn/view {:style style/mask-view}])}
          (when profile-picture
            [fast-image/fast-image
             {:accessibility-label :ring-background
              :style               style/picture-avatar-mask
              :source              profile-picture}])]]]
       [reanimated/view
        {:style (reanimated/apply-animations-to-style
                 {:opacity opacity}
                 style/user-name-container)}
        [quo/text
         {:size            :heading-2
          :weight          :semi-bold
          :number-of-lines 1
          :style           style/user-name} name]]
       [reanimated/text
        {:number-of-lines 1
         :style           (reanimated/apply-animations-to-style
                           {:color   user-hash-color
                            :opacity user-hash-opacity}
                           (text/text-style
                            {:weight :monospace
                             :style  style/user-hash}))} hash]
       [reanimated/view
        {:style [emoji-hash-style]}
        [quo/text
         {:weight          :monospace
          :number-of-lines 1
          :style           style/emoji-hash} emoji-hash]]]]]))

(defn profile-card
  [props]
  [:f> f-profile-card-component props])

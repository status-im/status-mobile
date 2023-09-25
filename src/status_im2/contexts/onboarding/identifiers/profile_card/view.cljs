(ns status-im2.contexts.onboarding.identifiers.profile-card.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.masked-view :as masked-view]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im2.contexts.onboarding.identifiers.profile-card.style :as style]
            [utils.worklets.identifiers-highlighting :as worklets.identifiers-highlighting]))

(defn- f-profile-card-component
  [{:keys [profile-picture name emoji-hash
           customization-color progress]
    :or   {customization-color :turquoise}
    :as   props}]
  (let [container-background (worklets.identifiers-highlighting/background
                              (colors/custom-color customization-color 50)
                              @progress)
        opacity              (worklets.identifiers-highlighting/opacity @progress)
        avatar-opacity       (worklets.identifiers-highlighting/avatar-opacity @progress)
        ring-opacity         (worklets.identifiers-highlighting/ring-opacity @progress)
        user-hash-color      (worklets.identifiers-highlighting/user-hash-color @progress)
        user-hash-opacity    (worklets.identifiers-highlighting/user-hash-opacity @progress)
        emoji-hash-style     (worklets.identifiers-highlighting/emoji-hash-style @progress)
        avatar               [quo/user-avatar
                              {:full-name           name
                               :profile-picture     profile-picture
                               :size                :medium
                               :status-indicator?   false
                               :customization-color customization-color}]]
    [rn/view
     {:style style/card-view}
     [reanimated/view
      {:style (style/card-container container-background)}
      [rn/view {:style style/card-header}
       [reanimated/view {:style (style/avatar avatar-opacity)} avatar]
       [masked-view/masked-view
        {:style        {:position :absolute}
         :mask-element (reagent/as-element
                        [reanimated/view {:style (style/mask-view ring-opacity)}])}
        (when profile-picture avatar)]]
      [reanimated/view
       {:style (style/user-name-container opacity)}
       [quo/text
        {:size            :heading-2
         :weight          :semi-bold
         :number-of-lines 1
         :style           style/user-name} name]]
      [reanimated/text
       {:number-of-lines 3
        :style           (style/user-hash user-hash-color user-hash-opacity)}
       (:hash props)]
      [reanimated/view
       {:style [emoji-hash-style]}
       [quo/text
        {:weight          :monospace
         :number-of-lines 1
         :style           style/emoji-hash}
        emoji-hash]]]]))

(defn profile-card
  [props]
  [:f> f-profile-card-component props])

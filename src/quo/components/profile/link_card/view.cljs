(ns quo.components.profile.link-card.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.profile.link-card.properties :as properties]
            [quo.components.profile.link-card.style :as style]
            [quo.components.utilities.social.view :as social]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]))

(defn view
  [{:keys [address on-press icon title customization-color container-style]}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:accessibility-label :link-card
      :on-press            on-press}
     [linear-gradient/linear-gradient
      {:colors [(properties/gradient-start-color theme customization-color)
                (properties/gradient-end-color theme customization-color)]
       :start  {:x 0 :y 1}
       :end    {:x 1 :y 1}
       :style  (merge (style/container theme) container-style)}
      [rn/view {:style style/icon-container}
       [social/view
        {:accessibility-label :social-icon
         :social              icon}]]
      [text/text
       {:accessibility-label :title
        :number-of-lines     1
        :weight              :semi-bold}
       title]
      [text/text
       {:accessibility-label :address
        :size                :paragraph-2
        :numberOfLines       1
        :style               (style/address theme)}
       address]]]))

(ns quo2.components.settings.data-item.content.subtitle
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as resources]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.icon :as icons]))

(defn view
  [theme size description icon icon-color blur? subtitle]
  (let [yellow-color [:yellow (if (= theme :dark) 60 50)]]
    [rn/view {:style style/subtitle-container}
     (when (not= :small size)
       [rn/view {:style (style/subtitle-icon-container description)}
        (case description
          :icon    [icons/icon icon
                    {:accessibility-label :description-icon
                     :size                16
                     :color               icon-color}]
          :account [account-avatar/view
                    {:customization-color (get-in colors/customization yellow-color)
                     :size                16
                     :emoji               "🎮"
                     :type                :defaul}]
          :network [rn/image
                    {:accessibility-label :description-image
                     :source              (resources/tokens :eth)
                     :style               style/image}]
          :default nil
          nil)])
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  (style/description blur?)}
      subtitle]]))

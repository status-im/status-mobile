(ns quo2.components.settings.data-item.content.subtitle
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as resources]
            [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.icon :as icons]))

(defn view
  [{:keys [theme size description icon icon-color blur? subtitle emoji-color emoji]}]
  (let [background-color (colors/theme-colors
                          (colors/custom-color emoji-color 50)
                          (colors/custom-color emoji-color 60)
                          theme)]
    [rn/view {:style style/subtitle-container}
     (when (not= :small size)
       [rn/view {:style (style/subtitle-icon-container description)}
        (case description
          :icon    [icons/icon icon
                    {:accessibility-label :description-icon
                     :size                16
                     :color               icon-color}]
          :account [account-avatar/view
                    {:customization-color background-color
                     :size                16
                     :emoji               emoji
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
       :style  (style/description blur? theme)}
      subtitle]]))

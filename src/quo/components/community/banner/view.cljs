(ns quo.components.community.banner.view
  (:require
    [quo.components.community.banner.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]))

(defn- card-title-and-description
  [title description theme]
  [rn/view {:style style/banner-content}
   [rn/view {:style style/banner-title}
    [text/text
     {:accessibility-label :community-name-text
      :ellipsize-mode      :tail
      :number-of-lines     1
      :weight              :semi-bold
      :size                :paragraph-1}
     title]
    [text/text
     {:accessibility-label :community-name-text
      :ellipsize-mode      :tail
      :number-of-lines     1
      :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
      :weight              :regular
      :size                :paragraph-2}
     description]]])

(defn view
  [{:keys [title description on-press accessibility-label banner style]}]
  (let [theme (quo.theme/use-theme)]
    [rn/pressable
     {:on-press            on-press
      :accessibility-label accessibility-label
      :style               (merge (style/community-card theme) style)}
     [card-title-and-description title description theme]
     [rn/image
      {:style               style/discover-illustration
       :source              banner
       :accessibility-label :discover-communities-illustration}]]))

(ns quo2.components.community.banner.view
  (:require [quo2.components.community.banner.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn- card-title-and-description
  [title description]
  [rn/view
   {:style style/banner-content}
   [rn/view
    {:style style/banner-title}
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
      :color               (colors/theme-colors
                            colors/neutral-50
                            colors/neutral-40)
      :weight              :regular
      :size                :paragraph-2}
     description]]])

(defn view
  [{:keys [title description on-press accessibility-label banner]}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view
    (merge (style/community-card 16)
           {:background-color (colors/theme-colors
                               colors/white
                               colors/neutral-90)}
           style/banner-card)
    [card-title-and-description title description]
    [rn/image
     {:style               style/discover-illustration
      :source              banner
      :accessibility-label :discover-communities-illustration}]]])

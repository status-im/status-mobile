(ns quo2.components.community.discover.view
  (:require [quo2.components.community.discover.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.common.resources :as resources]))

(defn card-title-and-description
  [title description]
  [rn/view
   {:flex           1
    :padding-top    8
    :padding-bottom 8
    :border-radius  12}
   [rn/view
    {:flex               1
     :padding-horizontal 12}
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

(defn discover-card
  [{:keys [title description on-press accessibility-label screen]}]
  [rn/touchable-without-feedback
   {:on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view
    (merge (style/community-card 16)
           {:background-color (colors/theme-colors
                               colors/white
                               colors/neutral-90)}
           {:flex-direction    :row
            :margin-horizontal 20
            :margin-vertical   8
            :height            56
            :padding-right     12})
    [card-title-and-description title description]
    [rn/image
     {:style               style/discover-illustration
      :source              (resources/get-image (if (= screen :communities)
                                                  :discover
                                                  :invite-friends))
      :accessibility-label :discover-communities-illustration}]]])

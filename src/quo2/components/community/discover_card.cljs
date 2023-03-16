(ns quo2.components.community.discover-card
  (:require [quo2.components.community.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

;; Discover card placeholders images.
;; TODO replaced when real data is available
(def images
  {:images [{:id 1 :column-images [{:id 1 :image ""}]}
            {:id 2 :column-images [{}]}]})

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

(defn placeholder-list-images
  [{:keys [images width height border-radius]}]
  [rn/view
   [rn/view {:justify-content :center}
    (for [{:keys [id]} images]
      ^{:key id}
      [rn/view
       {:border-radius    border-radius
        :margin-top       4
        :margin-right     4
        :width            width
        :height           height
        :background-color colors/neutral-10}])]])

(defn placeholder-row-images
  [{:keys [first-image last-image images width height
           border-radius]}]
  [rn/view
   (when first-image
     [rn/view
      {:border-bottom-right-radius 6
       :border-bottom-left-radius  6
       :margin-right               4
       :width                      width
       :height                     height
       :background-color           colors/neutral-10}])
   (when images
     [placeholder-list-images
      {:images        images
       :width         32
       :height        32
       :border-radius 6}])
   (when last-image
     [rn/view
      {:border-top-left-radius  border-radius
       :border-top-right-radius 6
       :margin-top              4
       :width                   width
       :height                  height
       :background-color        colors/neutral-10}])])

(defn discover-card
  [{:keys [title description on-press accessibility-label]}]
  (let [on-joined-images (get images :images)]
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
      (for [{:keys [id column-images]} on-joined-images]
        ^{:key id}
        [placeholder-row-images
         {:images        (when (= id 1)
                           column-images)
          :width         32
          :height        (if (= id 1) 8 26)
          :border-radius 6
          :first-image   "" ; TODO replace with real data
          :last-image    ""}])]])) ; TODO replace with real data

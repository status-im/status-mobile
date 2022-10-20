(ns quo2.components.community.discover-card
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.ui.screens.communities.styles :as styles]
            [status-im.ui.components.react :as react]))

;; Discover card placeholders images.
;; TODO replaced when real data is available
(def images
  {:images [{:id 1 :column-images [{:id 1   :image   ""}]}
            {:id 2 :column-images [{}]}]})

(defn card-title-and-description [title description]
  [react/view
   {:flex               1
    :padding-top        8
    :padding-bottom     8
    :border-radius      12}
   [react/view {:flex                1
                :padding-horizontal  12}
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :weight              :semi-bold
                :size                :paragraph-1}
     title]
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :color               (colors/theme-colors
<<<<<<< HEAD
                                      colors/neutral-50
                                      colors/neutral-40)
=======
                                       colors/neutral-50
                                       colors/neutral-40)
>>>>>>> c0e3854d3... feat: messages contact requests
                :weight               :regular
                :size                 :paragraph-2}
     description]]])

(defn placeholder-list-images [{:keys [images width height border-radius]}]
  [react/view
   [react/view {:justify-content    :center}
    (for [{:keys [id]} images]
      ^{:key id}
      [react/view {:border-radius    border-radius
                   :margin-top       4
                   :margin-right     4
                   :width            width
                   :height           height
                   :background-color colors/neutral-10}])]])

(defn placeholder-row-images [{:keys [first-image last-image images width height
                                      border-radius]}]
  [react/view
   (when first-image
     [react/view {:border-bottom-right-radius 6
                  :border-bottom-left-radius  6
                  :margin-right               4
                  :width                      width
                  :height                     height
                  :background-color           colors/neutral-10}])
   (when images
     [placeholder-list-images {:images        images
                               :width         32
                               :height        32
                               :border-radius 6}])
   (when last-image
     [react/view {:border-top-left-radius    border-radius
                  :border-top-right-radius   6
                  :margin-top                4
                  :width                     width
                  :height                    height
                  :background-color          colors/neutral-10}])])

(defn discover-card [{:keys [title description on-press]}]
  (let [on-joined-images     (get images   :images)]
    [react/touchable-without-feedback
     {:on-press on-press}
     [react/view (merge (styles/community-card 16)
                        {:background-color  (colors/theme-colors
<<<<<<< HEAD
                                             colors/white
                                             colors/neutral-90)}
=======
                                              colors/white
                                              colors/neutral-90)}
>>>>>>> c0e3854d3... feat: messages contact requests
                        {:flex-direction  :row
                         :margin-horizontal 20
                         :height          56
                         :padding-right   12})
      [card-title-and-description title description]
      (for [{:keys [id column-images]} on-joined-images]
        ^{:key id}
        [placeholder-row-images {:images        (when (= id 1)
                                                  column-images)
                                 :width         32
                                 :height        (if (= id 1) 8 26)
                                 :border-radius 6
<<<<<<< HEAD
                                 :first-image   "" ; TODO replace with real data
                                 :last-image    ""}]) ; TODO replace with real data
=======
                                 :first-image   "" ;; TODO replace with real data
                                 :last-image    ""}]) ;; TODO replace with real data
>>>>>>> c0e3854d3... feat: messages contact requests
      ]]))

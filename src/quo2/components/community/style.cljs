(ns quo2.components.community.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.shadows :as shadows]))

(defn community-card
  [radius theme]
  {:shadow-offset    {:width  0
                      :height 2}
   :shadow-radius    radius
   :shadow-opacity   1
   :shadow-color     colors/shadow
   :elevation        1
   :border-radius    radius
   :justify-content  :space-between
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90
                      theme)})

(def detail-container
  {:flex 1})

(defn stats-count-container
  []
  {:flex-direction :row
   :align-items    :center
   :margin-right   16})

(defn card-stats-container
  []
  {:flex-direction :row})

(defn list-stats-container
  []
  {:flex-direction :row
   :align-items    :center})

(defn card-stats-position
  []
  {:position :absolute
   :top      116
   :right    12
   :left     12})

(defn community-tags-position
  []
  {:position :absolute
   :top      154
   :right    12
   :left     12})

(defn card-view-content-container
  [padding-horizontal theme]
  {:position                :absolute
   :top                     40
   :bottom                  0
   :left                    0
   :right                   0
   :border-radius           20
   :padding-horizontal      padding-horizontal
   :border-top-right-radius 16
   :border-top-left-radius  16
   :background-color        (colors/theme-colors
                             colors/white
                             colors/neutral-90
                             theme)})

(defn card-view-chat-icon
  [icon-size theme]
  {:border-radius    48
   :position         :absolute
   :top              (- (/ icon-size 2))
   :left             (/ icon-size 4)
   :padding          2
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90
                      theme)})

(defn list-info-container
  []
  {:flex-direction     :row
   :border-radius      16
   :padding-horizontal 12
   :align-items        :center
   :padding-vertical   8})

(defn membership-info-container
  []
  {:flex-direction :row
   :border-radius  16
   :align-items    :center
   :height         48})

(defn community-title-description-container
  [margin-top]
  {:margin-top margin-top})

(defn community-cover-container
  [height]
  {:flex-direction          :row
   :height                  height
   :border-top-right-radius 20
   :border-top-left-radius  20
   :background-color        colors/primary-50-opa-20})

(defn permission-tag-styles
  []
  {:position :absolute
   :top      8
   :right    8})

(def token-tag-spacing
  {:margin-top   10
   :margin-right 8})

(defn token-row
  [padding?]
  (merge
   {:flex-direction :row
    :flex-wrap      :wrap
    :align-items    :center}
   (when padding?
     {:padding-horizontal 12})))

(defn token-row-or-text
  [padding? theme]
  (merge
   {:margin-top 4
    :color      (colors/theme-colors
                 colors/neutral-50
                 colors/neutral-40
                 theme)}
   (when padding?
     {:padding-left 12})))

(defn token-row-or-border
  [theme]
  {:height           1
   :background-color (colors/theme-colors
                      colors/neutral-20
                      colors/neutral-80
                      theme)})

(defn channel-action-touch
  [big?]
  {:flex      1
   :max-width (if big? 216 104)})

(defn channel-action
  [color]
  {:padding          12
   :height           102
   :flex             1
   :border-radius    16
   :background-color (colors/custom-color color 50 10)
   :justify-content  :space-between})

(def channel-action-row
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(defn loading-card
  [width theme]
  (merge
   {:width            width
    :height           230
    :border-radius    16
    :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)}
   (shadows/get 2 theme)))

(defn loading-avatar
  [theme]
  {:width            48
   :height           48
   :border-radius    24
   :position         :absolute
   :top              -24
   :left             12
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :border-color     (colors/theme-colors colors/white colors/neutral-90 theme)
   :border-width     2})

(defn loading-lock
  [theme]
  {:width            48
   :height           24
   :border-radius    20
   :position         :absolute
   :top              8
   :right            8
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})


(defn loading-cover-container
  [theme]
  {:flex-direction          :row
   :height                  64
   :border-top-right-radius 16
   :border-top-left-radius  16
   :background-color        (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn loading-content-container
  [theme]
  {:position           :absolute
   :heigth             190
   :top                40
   :bottom             0
   :left               0
   :right              0
   :padding-horizontal 12
   :border-radius      16
   :background-color   (colors/theme-colors colors/white colors/neutral-90 theme)})

(def loading-tags-container
  {:margin-top 20 :flex-direction :row :align-items :center})

(def loading-stats-container
  {:margin-top 20 :flex-direction :row :align-items :center})

(defn loading-stat-circle
  [theme margin-left]
  {:height           14
   :width            14
   :border-radius    7
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :margin-left      margin-left})

(defn loading-stat-line
  [theme margin-left]
  {:height           12
   :width            50
   :border-radius    5
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :margin-left      margin-left})

(defn loading-tag
  [theme margin-left]
  {:height           24
   :width            76
   :border-radius    20
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :margin-left      margin-left})

(defn loading-content-line
  [{:keys [theme width margin-top]}]
  {:width            width
   :height           16
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-70 theme)
   :border-radius    5
   :margin-top       margin-top})

(def loading-card-content-container {:margin-top 36})

(def notification-container
  {:width       20
   :display     :flex
   :align-items :center})

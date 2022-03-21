(ns status-im.ui.screens.chat.message.styles
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as photos]))

(defn picker-wrapper-style [{:keys [display-photo? outgoing timeline]}]
  (merge {:flex-direction :row
          :flex           1
          :padding-top    4
          :padding-right  8}
         (if outgoing
           {:justify-content :flex-end}
           {:justify-content :flex-start})
         (when-not timeline
           (if display-photo?
             {:padding-left (+ 16 photos/default-size)}
             {:padding-left 8}))))

(defn container-style [{:keys [outgoing timeline]}]
  (merge {:border-top-left-radius     16
          :border-top-right-radius    16
          :border-bottom-right-radius 16
          :border-bottom-left-radius  16
          :background-color           (:ui-background @colors/theme)}
         (if timeline
           {:border-top-left-radius 16
            :border-top-right-radius 4}
           (if outgoing
             {:border-top-right-radius 4}
             {:border-top-left-radius 4}))))

(defn reactions-picker-row []
  {:flex-direction     :row
   :padding-vertical   8
   :padding-horizontal 8})

(defn quick-actions-container []
  {:flex-direction   :column
   :justify-content  :space-evenly
   :border-top-width 1
   :border-top-color (:ui-01 @colors/theme)})

(defn quick-actions-row []
  {:flex-direction     :row
   :padding-horizontal 16
   :padding-vertical   12
   :justify-content    :space-between
   :border-top-width   1
   :border-top-color   (:ui-01 @colors/theme)})

(defn reaction-style [{:keys [outgoing own]}]
  (merge {:border-top-left-radius     10
          :border-top-right-radius    10
          :border-bottom-right-radius 10
          :border-bottom-left-radius  10
          :flex-direction             :row
          :margin-vertical            2
          :padding-right              8
          :padding-left               2
          :padding-vertical           2}
         (if own
           {:background-color (:interactive-01 @colors/theme)}
           {:background-color (:interactive-02 @colors/theme)})
         (if outgoing
           {:border-top-right-radius 2
            :margin-left             4}
           {:border-top-left-radius 2
            :margin-right           4})))

(defn reaction-quantity-style [{:keys [own]}]
  {:font-size   12
   :line-height 16
   :color       (if own
                  colors/white
                  (:text-01 @colors/theme))})

(defn reactions-row [{:keys [outgoing display-photo?]} timeline]
  (merge {:flex-direction :row
          :padding-right  8}
         (if (and outgoing (not timeline))
           {:justify-content :flex-end}
           {:justify-content :flex-start})
         (if (or display-photo? timeline)
           {:padding-left (+ 30 photos/default-size (when timeline 8))}
           {:padding-left 30})))

(defn reaction-button [active]
  (merge {:width             40
          :height            40
          :border-radius     20
          :justify-content   :center
          :align-items       :center
          :margin-horizontal 1
          :border-width      1
          :border-color      :transparent}
         (when active
           {:background-color (:interactive-02 @colors/theme)
            ;; FIXME: Use broder color here
            :border-color     "rgba(67, 96, 223, 0.2)"})))

(defn link-preview-request-wrapper []
  {:border-radius    16
   :border-width     1
   :border-color     colors/gray-lighter
   :margin-vertical  4
   :background-color (:ui-background @colors/theme)})

(def link-preview-request-image
  {:width 132
   :height 94
   :align-self :center})

(def community-preview-header
  {:margin 8 :margin-left 12})

(defn link-preview-wrapper [outgoing timeline]
  {:overflow                   :hidden
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-left-radius  (if timeline 16 (if outgoing 16 4))
   :border-bottom-right-radius (if timeline 16 (if outgoing 4 16))
   :border-width               1
   :border-color               colors/gray-lighter
   :background-color           colors/white
   :margin-vertical            4})

(def screen-width
  (-> "window"
      react/get-dimensions
      :width))

(defn scale-dimensions
  "Scale a given height and width to be maximum percentage allowed of the screen width"
  [{:keys [height width] :as dimensions}]
  (let [max-cover    0.5
        aspect-ratio (/ height width)
        max-width    (* max-cover screen-width)]
    (if (< width max-width)
      dimensions
      {:width  max-width
       :height (* aspect-ratio max-width)})))

(defn link-preview-image [outgoing {:keys [height width] :as dimensions}]
  (merge (if (and (pos? height)
                  (pos? width))
           (scale-dimensions dimensions)
           {:height 170})
         {:overflow                   :hidden
          :border-top-left-radius     16
          :border-top-right-radius    16
          :border-bottom-left-radius  (if outgoing 16 4)
          :border-bottom-right-radius (if outgoing 4 16)}))

(def link-preview-title
  {:margin-horizontal 12
   :margin-top        10})

(def link-preview-site
  {:margin-horizontal 12
   :margin-top        2
   :margin-bottom     10})

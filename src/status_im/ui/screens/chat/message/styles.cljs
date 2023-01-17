(ns status-im.ui.screens.chat.message.styles
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as photos]))

(defn container-style
  [{:keys [outgoing timeline]}]
  (merge {:border-top-left-radius     16
          :border-top-right-radius    16
          :border-bottom-right-radius 16
          :border-bottom-left-radius  16
          :background-color           (:ui-background @colors/theme)}
         (if timeline
           {:border-top-left-radius  16
            :border-top-right-radius 4}
           (if outgoing
             {:border-top-right-radius 4}
             {:border-top-left-radius 4}))))

(def screen-width
  (-> "window"
      react/get-dimensions
      :width))

(defn reactions-row
  [timeline margin-top]
  {:flex-direction  :row
   :padding-right   8
   :padding-bottom  8
   :justify-content :flex-start
   :margin-top      margin-top
   :flex-wrap       :wrap
   :max-width       (- screen-width (+ 30 photos/default-size (when timeline 8)))
   :margin-left     (+ 30 photos/default-size (when timeline 8))})

(def community-preview-header
  {:margin 8 :margin-left 12})

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

(defn link-preview-image
  [outgoing {:keys [height width] :as dimensions}]
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

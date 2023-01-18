(ns status-im2.contexts.chat.messages.link-preview.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [status-im.ui.components.react :as react]))

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

(defn link-preview-enable-request-wrapper
  []
  {:border-left-width  2
   :border-left-color  (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :padding-horizontal 16
   :margin-top         8})

(defn wrapper
  []
  {:overflow           :hidden
   :border-left-width  2
   :border-left-color  (colors/theme-colors colors/neutral-10 colors/neutral-60)
   :padding-horizontal 16
   :margin-top         8})

(defn separator
  []
  {:height 4})

(defn title-wrapper
  []
  {:flex-direction :row
   :align-items    :center})

(defn title-site-image
  []
  {:width            16
   :height           16
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-60)
   :border-radius    8
   :margin-right     6})

(defn title-text
  []
  (merge
   {:color (colors/theme-colors colors/black colors/white)}
   typography/font-semi-bold
   typography/paragraph-1))

(defn main-text
  []
  (merge
   {:color         (colors/theme-colors colors/black colors/white)
    :margin-top    4
    :margin-bottom 8}
   typography/paragraph-2))

(defn extra-text
  []
  (merge
   {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}
   typography/font-medium
   typography/paragraph-2))

(defn image
  [{:keys [height width] :as dimensions}]
  (merge (if (and (pos? height)
                  (pos? width))
           (scale-dimensions dimensions)
           {:height 170})
         {:overflow      :hidden
          :border-radius 12}))


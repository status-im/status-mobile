(ns quo2.components.selectors.react-selector.style
  (:require [quo2.foundations.colors :as colors]))

(defn add-reaction
  [pinned? theme]
  (let [border-color (if pinned?
                       (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                       (colors/theme-colors colors/neutral-20 colors/neutral-70 theme))]
    (cond-> {:justify-content    :center
             :align-items        :center
             :padding-horizontal 7
             :border-radius      8
             :border-width       1
             :height             24
             :border-color       border-color})))

(defn reaction
  [pressed? pinned? theme]
  (let [background-color (cond
                           (not pressed?) :transparent
                           pinned?        (colors/theme-colors colors/neutral-80-opa-5
                                                               colors/white-opa-5
                                                               theme)
                           :else          (colors/theme-colors colors/neutral-10
                                                               colors/neutral-80-opa-40
                                                               theme))
        border-color     (if pinned?
                           (colors/theme-colors colors/neutral-80-opa-5
                                                colors/white-opa-5
                                                theme)
                           (colors/theme-colors colors/neutral-20
                                                colors/neutral-80
                                                theme))]
    {:flex-direction     :row
     :justify-content    :center
     :align-items        :center
     :padding-horizontal 8
     :border-radius      8
     :height             24
     :border-width       1
     :background-color   background-color
     :border-color       border-color}))

(def reaction-count {:margin-left 4})

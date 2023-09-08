(ns quo2.components.code.snippet-preview.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.components.code.code.style :as code-style]))

(defn container
  [theme]
  {:overflow         :hidden
   :width            108
   :background-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-radius    8})

(defn line-number-container
  [theme]
  {:position         :absolute
   :bottom           0
   :top              0
   :left             0
   :width            20
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-70 theme)})

(defn text-style
  [class-names]
  (let [text-color (->> class-names
                        (map keyword)
                        (some (fn [class-name]
                                (when-let [text-color (code-style/theme class-name)]
                                  text-color))))]
    (cond-> {:flex-shrink 1
             :line-height 18}
      text-color (assoc :color text-color))))

(def line {:flex 1 :flex-direction :row})

(defn line-number
  [width]
  {:width           width
   :align-items     :center
   :justify-content :center})


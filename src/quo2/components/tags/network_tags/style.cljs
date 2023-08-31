(ns quo2.components.tags.network-tags.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [{:keys [status theme blur?]}]
  {:flex-direction   :row
   :align-self       :flex-start
   :background-color (when (= status :error)
                       (colors/theme-colors
                        (colors/custom-color :danger 50 10)
                        (colors/custom-color :danger 60 10)
                        theme))
   :border-width     1
   :border-color     (cond (= status :error)
                           (colors/theme-colors
                            (colors/custom-color :danger 50 20)
                            (colors/custom-color :danger 60 20)
                            theme)
                           (and blur? (= status :default)) (colors/theme-colors
                                                            colors/neutral-80-opa-5
                                                            colors/white-opa-5
                                                            theme)
                           :else (colors/theme-colors
                                  colors/neutral-20
                                  colors/neutral-80
                                  theme))
   :border-radius    8
   :padding-left     5
   :padding-right    5
   :padding-top      3
   :padding-bottom   2})

(defn title-style
  [{:keys [status theme]}]
  {:padding-left 4
   :margin-top   -1
   :color        (when (= status :error)
                   (colors/theme-colors
                    (colors/custom-color :danger 50)
                    (colors/custom-color :danger 60)
                    theme))})

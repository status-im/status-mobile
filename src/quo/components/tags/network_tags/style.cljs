(ns quo.components.tags.network-tags.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [{:keys [status theme blur?]}]
  {:flex-direction   :row
   :align-self       :flex-start
   :background-color (when (= status :error)
                       (colors/theme-colors
                        (colors/override-color :danger 10 50)
                        (colors/override-color :danger 10 60)
                        theme))
   :border-width     1
   :border-color     (cond (= status :error)
                           (colors/theme-colors
                            (colors/override-color :danger 20 50)
                            (colors/override-color :danger 20 60)
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
  [{:keys [status theme networks-shown?]}]
  {:padding-left (if networks-shown? 4 0)
   :margin-top   -1
   :color        (when (= status :error)
                   (colors/resolve-color :danger theme))})

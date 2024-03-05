(ns quo.foundations.gradients
  (:require
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.linear-gradient :as linear-gradient]))

(defn- gradient-colors
  [index theme]
  (case index
    1 [(colors/resolve-color :yellow theme)
       (colors/resolve-color :sky theme)
       (colors/resolve-color :purple theme)]
    2 [(colors/resolve-color :orange theme)
       (colors/resolve-color :purple theme)
       (colors/resolve-color :blue theme)]
    3 [(colors/resolve-color :blue theme)
       (colors/resolve-color :magenta theme)
       (colors/resolve-color :yellow theme)]
    4 [(colors/resolve-color :army theme)
       (colors/resolve-color :orange theme)
       (colors/resolve-color :blue theme)]
    [(colors/resolve-color :purple theme)
     (colors/resolve-color :army theme)
     (colors/resolve-color :yellow theme)]))

(defn view
  [{:keys [color-index container-style] :or {color-index 1}}]
  (let [theme (quo.theme/use-theme-value)]
    [linear-gradient/linear-gradient
     {:style               (assoc container-style
                                  :border-radius
                                  16)
      :accessibility-label :gradient-overlay
      :colors              (gradient-colors color-index theme)
      :start               {:x 0 :y 0}
      :end                 {:x 0 :y 1}}]))

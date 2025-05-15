(ns quo.foundations.gradients
  (:require
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.linear-gradient :as linear-gradient]))

(defn- gradient-colors
  [index theme]
  (case index
    :gradient-1 [(colors/resolve-color :yellow theme)
                 (colors/resolve-color :sky theme)
                 (colors/resolve-color :purple theme)]
    :gradient-2 [(colors/resolve-color :orange theme)
                 (colors/resolve-color :purple theme)
                 (colors/resolve-color :blue theme)]
    :gradient-3 [(colors/resolve-color :blue theme)
                 (colors/resolve-color :magenta theme)
                 (colors/resolve-color :yellow theme)]
    :gradient-4 [(colors/resolve-color :army theme)
                 (colors/resolve-color :orange theme)
                 (colors/resolve-color :blue theme)]
    [(colors/resolve-color :purple theme)
     (colors/resolve-color :army theme)
     (colors/resolve-color :yellow theme)]))

(defn view
  [{:keys [color-index container-style] :or {color-index 1}}]
  (let [theme (quo.context/use-theme)]
    [linear-gradient/linear-gradient
     {:style               (merge {:border-radius 16} container-style)
      :accessibility-label :gradient-overlay
      :colors              (gradient-colors color-index theme)
      :start               {:x 0 :y 0}
      :end                 {:x 0 :y 1}}]))

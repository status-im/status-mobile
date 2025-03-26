(ns quo.components.common.new-feature-gradient
  (:require [quo.context]
            [quo.foundations.colors :as colors]
            [react-native.linear-gradient :as linear-gradient]))

(defn view
  [{:keys [style accessibility-label]}]
  (let [theme (quo.context/use-theme)]
    [linear-gradient/linear-gradient
     {:style               style
      :accessibility-label accessibility-label
      :colors              [(colors/resolve-color :yellow theme)
                            colors/new-feature-gradient-pink]
      :use-angle           true
      :angle               78
      :angle-center        {:x 0.5 :y 0.5}}]))

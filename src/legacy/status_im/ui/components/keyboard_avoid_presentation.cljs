(ns legacy.status-im.ui.components.keyboard-avoid-presentation
  (:require
    [legacy.status-im.ui.components.react :as react]
    [react-native.utils :as rn.utils]
    [reagent.core :as reagent]))

(defn keyboard-avoiding-view
  [& argv]
  (let [[props children] (rn.utils/get-props-and-children argv)]
    (reagent/as-element
     (into [react/keyboard-avoiding-view
            (update props
                    :keyboard-vertical-offset
                    +
                    20
                    (if (:ignore-offset props) 44 0))]
           children))))

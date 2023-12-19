(ns legacy.status-im.ui.components.keyboard-avoid-presentation
  (:require
    [legacy.status-im.ui.components.react :as react]
    [reagent.core :as reagent]))

(defn keyboard-avoiding-view
  []
  (let [this     (reagent/current-component)
        props    (reagent/props this)
        children (reagent/children this)]
    (reagent/as-element
     (into [react/keyboard-avoiding-view
            (update props
                    :keyboardVerticalOffset
                    +
                    20
                    (if (:ignore-offset props) 44 0))]
           children))))

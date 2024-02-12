(ns legacy.status-im.ui.components.keyboard-avoid-presentation
  (:require
    [legacy.status-im.ui.components.react :as react]
    [utils.reagent :as reagent]))

(defn keyboard-avoiding-view
  [& this]
  (let [props    (apply reagent/props this)
        children (apply reagent/children this)]
    (reagent/as-element
     (into [react/keyboard-avoiding-view
            (update props
                    :keyboardVerticalOffset
                    +
                    20
                    (if (:ignore-offset props) 44 0))]
           children))))

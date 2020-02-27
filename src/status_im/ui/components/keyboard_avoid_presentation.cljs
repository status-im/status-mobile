(ns status-im.ui.components.keyboard-avoid-presentation
  (:require [status-im.ui.components.react :as react]
            [oops.core :refer [oget]]
            [reagent.core :as reagent]))

(defn keyboard-avoiding-view []
  (let [this     (reagent/current-component)
        props    (reagent/props this)
        children (reagent/children this)]
    [react/safe-area-consumer
     (fn [insets]
       (let [vertical-offset (+ (oget insets "top")
                               ;; 10 is the margin-top for presentation modal
                                10)]
         (reagent/as-element
          (into [react/keyboard-avoiding-view (merge props
                                                     {:keyboard-vertical-offset vertical-offset})]
                children))))]))

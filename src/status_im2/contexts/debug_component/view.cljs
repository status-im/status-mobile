(ns status-im2.contexts.debug-component.view
  (:require [react-native.core :as rn]
            [utils.re-frame :as rf]))

(def ^:private container
  {:flex               1
   :padding-horizontal 20
   :justify-content    :center})

(defn view
  []
  (let [debug-view (rf/sub [:debug/component])]
    [rn/view {:style container}
     (when-not (nil? debug-view)
       debug-view)]))

(ns status-im2.contexts.debug.view
  (:require [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [debug-view (rf/sub [:debug/view])]
    [rn/view
     {:style {:flex               1
              :padding-horizontal 20
              :justify-content    :center}}
     (when (not (nil? debug-view))
       debug-view)]))

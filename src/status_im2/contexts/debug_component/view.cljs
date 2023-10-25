(ns status-im2.contexts.debug-component.view
  (:require
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(def ^:private container-style
  {:flex               1
   :padding-horizontal 20
   :padding-top        80
   :align-items        :center})

(defn view
  []
  (let [component (rf/sub [:debug/component])]
    [rn/view
     {:style container-style}
     (when component
       component)]))

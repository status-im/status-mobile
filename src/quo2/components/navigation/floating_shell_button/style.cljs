(ns quo2.components.navigation.floating-shell-button.style
  (:require [react-native.reanimated :as reanimated]))

(defn floating-shell-button
  [style opacity-anim]
  (reanimated/apply-animations-to-style
   (if opacity-anim
     {:opacity opacity-anim}
     {})
   (merge
    {:flex-direction     :row
     :height             44
     :align-self         :center
     :padding-top        8
     :padding-horizontal 12
     :pointer-events     :box-none}
    style)))

(def right-section
  {:position       :absolute
   :flex-direction :row
   :right          0})

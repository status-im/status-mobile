(ns status-im2.contexts.profile.edit.style)

(defn header-container
  [inset]
  {:padding-top inset})

(def screen-container
  {:padding-top        12
   :padding-horizontal 20})

(def avatar-wrapper
  {:width         88
   :margin-top    20
   :margin-bottom 8})

(def camera-button
  {:position :absolute
   :right    0
   :bottom   0})

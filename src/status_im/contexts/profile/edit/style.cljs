(ns status-im.contexts.profile.edit.style)

(defn page-wrapper
  [inset]
  {:padding-top        inset
   :padding-horizontal 1})

(def screen-container
  {:padding-top        14
   :padding-horizontal 20})

(def avatar-wrapper
  {:width         88
   :margin-top    22
   :margin-bottom 12})

(def camera-button
  {:position      :absolute
   :border-radius 16
   :overflow      :hidden
   :right         0
   :bottom        0})

(def item-container
  {:padding-top 14})

(ns status-im.contexts.profile.settings.header.style)

(def avatar-row-wrapper
  {:display         :flex
   :padding-left    20
   :padding-right   12
   :margin-top      -60
   :margin-bottom   -4
   :align-items     :flex-end
   :justify-content :space-between
   :flex-direction  :row})

(defn header-middle-shape
  [background-color]
  {:background-color background-color
   :height           48
   :flex-grow        1})

(defn radius-container
  [opacity-animation]
  {:opacity         opacity-animation
   :flex-direction  :row
   :justify-content :space-between})

(ns status-im.contexts.communities.actions.generic-menu.style)

(defn container
  [max-height]
  {:flex         1
   :max-height   max-height
   :margin-left  20
   :margin-right 20})

(defn scroll-view-style
  [max-height]
  {:margin-bottom 12
   :max-height    max-height})

(def inner-container
  {:display         :flex
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def community-tag
  {:margin-right :auto
   :margin-top   4})

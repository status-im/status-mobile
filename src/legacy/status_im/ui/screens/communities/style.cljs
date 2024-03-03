(ns legacy.status-im.ui.screens.communities.style
  (:require
    [react-native.safe-area :as safe-area]))

(def contact-selection-heading
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :flex-end
   :margin-top      24
   :margin-bottom   16})

(def chat-button
  {:position         :absolute
   :bottom           (+ 12 (safe-area/get-bottom))
   :left             20
   :right            20})

(defn no-contacts
  []
  {:margin-bottom   (+ 96 (safe-area/get-bottom))
   :flex            1
   :justify-content :center
   :align-items     :center})

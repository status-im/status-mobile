(ns status-im.contexts.chat.messenger.composer.actions.style
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.composer.constants :as constants]))

(def actions-container
  {:height          constants/actions-container-height
   :justify-content :space-between
   :align-items     :center
   :z-index         2
   :flex-direction  :row})

(defn send-button
  [opacity z-index]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:position         :absolute
    :right            0
    :z-index          z-index
    :padding-vertical 3
    :padding-left     2}))

(defn record-audio-container
  []
  {:align-items      :center
   :background-color :transparent
   :flex-direction   :row
   :position         :absolute
   :left             -20
   :right            -20
   :bottom           0
   :height           constants/composer-default-height})

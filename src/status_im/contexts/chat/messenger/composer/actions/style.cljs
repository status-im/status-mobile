(ns status-im.contexts.chat.messenger.composer.actions.style
  (:require
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]))

(def actions-container
  {:height          constants/actions-container-height
   :justify-content :space-between
   :align-items     :center
   :flex-direction  :row})

(defn send-button
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:position         :absolute
    :right            0
    :padding-vertical 3
    :padding-left     2}))


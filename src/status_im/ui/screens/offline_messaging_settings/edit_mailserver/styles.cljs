(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]))

(def edit-mailserver-view
  {:flex              1
   :margin-horizontal 16
   :margin-vertical   15})

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :justify-content   :space-between
   :border-radius     styles/border-radius
   :height            52
   :margin-top        15})

(defstyle input
  {:flex               1
   :font-size          15
   :letter-spacing     -0.2
   :android            {:padding 0}})

(def qr-code
  {:margin-right 14})

(def bottom-container
  {:flex-direction    :row
   :margin-horizontal 12
   :margin-vertical   15})

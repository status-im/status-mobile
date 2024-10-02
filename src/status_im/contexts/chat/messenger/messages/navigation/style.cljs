(ns status-im.contexts.chat.messenger.messages.navigation.style
  (:require [quo.foundations.colors :as colors]
            [status-im.contexts.chat.messenger.messages.constants :as messages.constants]))

(defn navigation-view
  [navigation-view-height]
  {:top            0
   :left           0
   :right          0
   :position       :absolute
   :pointer-events :box-none
   :height         (+ navigation-view-height messages.constants/pinned-banner-height)
   :z-index        1})

(defn background
  [navigation-view-height]
  {:height   navigation-view-height
   :top      0
   :left     0
   :right    0
   :overflow :hidden
   :position :absolute})

(defn header-container
  [top-insets]
  {:margin-top         top-insets
   :flex-direction     :row
   :padding-horizontal 20
   :overflow           :hidden
   :height             messages.constants/top-bar-height
   :align-items        :center})

;;;; Content

(def header-content-container
  {:flex-direction    :row
   :align-items       :center
   :flex              1
   :margin-horizontal 12
   :height            40})

(def header-text-container
  {:margin-left 8})

(defn header-display-name
  [theme]
  {:color (colors/theme-colors colors/black colors/white theme)})

(defn header-status
  [theme]
  {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-40 theme)})

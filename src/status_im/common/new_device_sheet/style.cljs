(ns status-im.common.new-device-sheet.style
  (:require [quo.foundations.colors :as colors]))

(def heading
  {:padding-left   20
   :padding-bottom 8})

(def message
  {:padding-horizontal 20
   :padding-top        4})

(def warning
  {:margin-horizontal 20
   :margin-top        10})

(def drawer-container
  {:padding-horizontal 13
   :padding-top        16})

(def settings-subtext
  {:color         colors/white-opa-70
   :align-self    :center
   :margin-bottom 12})

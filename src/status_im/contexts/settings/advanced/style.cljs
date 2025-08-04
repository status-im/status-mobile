(ns status-im.contexts.settings.advanced.style
  (:require [react-native.safe-area :as safe-area]))

(def header
  {:top         0
   :left        0
   :right       0
   :padding-top safe-area/top})

(def advanced-item {:padding-top 8})

(def waku-backup-toggle
  {:padding-horizontal 20})

(def option-picker-sheet
  {:padding-horizontal 20
   :row-gap            8})

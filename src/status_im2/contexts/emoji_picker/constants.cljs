(ns status-im2.contexts.emoji-picker.constants
  (:require [react-native.core :as rn]))

(def search-debounce-ms 600)

(def emojis-per-row 7)

(def emoji-size 32)

(def emoji-row-padding-horizontal 20)

(def emoji-section-header-margin-bottom 6)

(def emoji-row-separator-height 16)

(def emoji-item-margin-right
  (/ (- (:width (rn/get-window)) (* emoji-row-padding-horizontal 2) (* emoji-size emojis-per-row))
     (- emojis-per-row 1)))

(def get-item-height
  (+ emoji-size emoji-row-separator-height))

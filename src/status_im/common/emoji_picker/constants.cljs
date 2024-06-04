(ns status-im.common.emoji-picker.constants
  (:require
    [react-native.core :as rn]))

(def ^:const default-category :people)

(def ^:const search-debounce-ms 600)

(def ^:const emojis-per-row 7)

(def ^:const emoji-size 32)

(def ^:const emoji-row-padding-horizontal 20)

(def ^:const emoji-section-header-margin-bottom 6)

(def ^:const emoji-row-separator-height 16)

(def ^:const categories-selector-height 56)

(def ^:const emoji-item-margin-right
  (/ (- (:width (rn/get-window))
        (* emoji-row-padding-horizontal 2)
        (* emoji-size emojis-per-row))
     (dec emojis-per-row)))

(def ^:const item-height (+ emoji-size emoji-row-separator-height))

(ns status-im.contexts.shell.share.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(def screen-padding 20)

(def header-row
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-button
  {:margin-bottom    12
   :background-color colors/white-opa-5})

(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(def qr-code-container
  {:margin-horizontal 20
   :margin-top        8})

(def emoji-hash-container
  {:border-radius     16
   :margin-top        12
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :flex-direction    :row
   :justify-content   :flex-start
   :align-items       :flex-start})

(def emoji-address-column
  {:flex-direction :column})

(def emoji-address-container
  {:flex-direction  :row
   :justify-content :flex-start})

(def emoji-hash-label
  {:color          colors/white-opa-40
   :margin-top     8
   :padding-bottom (if platform/ios? 2 0)
   :padding-left   12})

(def emoji-share-button-container
  {:position :absolute
   :right    0
   :top      12})

(def emoji-hash-content
  {:color          colors/white
   :align-self     :flex-start
   :padding-top    2
   :padding-bottom 12
   :padding-left   12
   :font-size      12})

(def tabs-container
  {:padding-horizontal screen-padding
   :margin-top         8
   :margin-bottom      16})



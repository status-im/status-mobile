(ns status-im.contexts.wallet.add-account.create-account.new-keypair.backup-recovery-phrase.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn seed-phrase-container
  [theme]
  {:margin-horizontal  20
   :padding-horizontal 12
   :border-width       1
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :border-radius      16
   :background-color   (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)
   :flex-direction     :row})

(def word-item
  {:align-items    :center
   :flex-direction :row})

(defn separator
  [theme]
  {:margin-vertical    12
   :margin-horizontal  12
   :border-width       (when platform/ios? 1)
   :border-right-width (when platform/android? 1)
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :border-style       :dashed})

(def step-item
  {:flex-direction  :row
   :margin-vertical 8
   :align-items     :center})

(def blur-container
  {:position      :absolute
   :left          0
   :right         0
   :top           0
   :bottom        0
   :border-radius 16
   :overflow      :hidden})

(defn blur
  [theme]
  {:style       {:flex 1}
   :blur-radius 25
   :blur-type   theme
   :blur-amount 20})

(def slide-button
  {:position :absolute
   :bottom   12
   :left     0
   :right    0})

(defn description-text
  [theme]
  {:margin-horizontal 40
   :text-align        :center
   :color             (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)})

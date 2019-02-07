(ns status-im.ui.screens.profile.tribute-to-talk.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as common.styles]))

(def intro-container
  {:flex              1
   :align-items       :center
   :justify-content   :space-between
   :margin-horizontal 32})

(def intro-image
  {:width           246
   :height          219
   :flex            1
   :margin-vertical 32
   :align-items     :center
   :justify-content :center
   :align-self      :center})

(def intro-text
  {:text-align  :center
   :font-size   22
   :color       colors/black
   :font-weight :bold
   :line-height 28})

(def description-label
  {:line-height    21
   :font-size      15
   :text-align     :center
   :color          colors/gray})

(def intro-button
  {:margin-vertical    8
   :padding-horizontal 32
   :align-self         :center
   :justify-content    :center
   :align-items        :center})

(def bottom-toolbar
  {:height 60})

(def step-n
  {:margin-top 5
   :font-size  14
   :text-align :center
   :color      colors/gray})

(def tribute-to-talk
  {:font-weight    :bold
   :font-size      15
   :color          colors/black
   :letter-spacing -0.2
   :text-align     :center})

(def container
  (merge
   common.styles/flex
   {:background-color colors/white}))

(def number-row
  {:flex            1
   :flex-direction  :row
   :justify-content :space-around})

(def number-container
  {:width             64
   :height            64
   :border-radius     32
   :margin-horizontal 8
   :margin-vertical   6
   :justify-content   :center
   :align-items       :center
   :background-color  colors/blue-light})

(def number
  {:line-height 28
   :font-size   22
   :color       colors/blue})

(def snt-amount-container
  {:margin-vertical   16
   :justify-content   :center
   :align-items       :center})

(def snt-amount
  {:font-size 32
   :color     colors/black})

(def snt-amount-label
  (assoc snt-amount :color colors/gray))

(def snt-asset-value
  {:font-size   15
   :font-weight "500"
   :line-height 22
   :color       colors/gray})

(def personalized-message-input
  {:border-radius       8
   :background-color    colors/gray-lighter
   :text-align-vertical :top
   :padding-horizontal  16
   :padding-vertical    17})

(def edit-view-message
  {:border-radius       8
   :background-color    colors/blue-light
   :color               colors/black
   :margin-horizontal   72
   :height              56
   :margin-bottom       16
   :text-align-vertical :top
   :padding-horizontal  12
   :padding-vertical    8})

(def finish-label
  {:font-size   22
   :line-height 28
   :font-weight :bold
   :text-align  :center
   :color       colors/black})

(defn finish-circle [color radius]
  {:background-color color
   :width            (* 2 radius)
   :height           (* 2 radius)
   :border-radius    radius
   :justify-content  :center
   :align-items      :center})

(defn icon-view [color]
  {:background-color color
   :justify-content  :center
   :align-items      :center
   :border-radius    20
   :width            40
   :height           40})

(def current-snt-amount
  {:font-size   28
   :line-height 28
   :color       colors/black
   :font-weight "500"})

(def edit-label
  {:font-size 15
   :color     colors/blue})

(def edit-note
  {:font-size         15
   :color             colors/gray
   :margin-top        16
   :margin-horizontal 16
   :text-align        :center})

(def enabled-note
  {:border-radius     8
   :border-width      1
   :padding           14
   :border-color      colors/gray-lighter
   :margin-horizontal 24
   :margin-bottom     50
   :flex-direction    :row})

(def learn-more-container
  {:flex-grow       1
   :justify-content :flex-start})

(def learn-more-image
  {:width           120
   :height          110
   :margin-top      32
   :margin-left     32
   :margin-bottom   16
   :align-items     :flex-start
   :justify-content :center})

(def learn-more-title-text
  (assoc intro-text
         :text-align  :left
         :margin-left 32))

(def learn-more-text-container-1
  {:margin-horizontal 32
   :margin-top        12
   :margin-bottom     24})

(def learn-more-text-container-2
  {:margin-horizontal 32
   :margin-top        16
   :margin-bottom     32})

(def learn-more-text
  {:font-size   17
   :line-height 22
   :color       colors/black})

(def learn-more-section
  {:border-radius     12
   :border-width      1
   :margin-horizontal 32
   :padding           8
   :width             238
   :border-color      colors/gray-lighter})

(def chat-sample-bubble
  {:background-color   colors/blue-light
   :padding-horizontal 12
   :padding-vertical   8
   :width              222
   :margin-top         4
   :border-radius      8})

(def edit-screen-top-row
  {:flex-direction    :row
   :margin-vertical   16
   :margin-horizontal 16
   :align-items       :flex-start
   :justify-content   :space-between})

(def remove-view
  {:flex-direction    :row
   :justify-content   :flex-start
   :align-self        :flex-start
   :margin-horizontal 16
   :margin-top        52})

(def remove-text
  {:color               colors/red
   :margin-left         16
   :font-size           17})

(def remove-note
  {:font-size  15
   :color      colors/gray
   :text-align :center
   :margin-top 12})

(def enabled-note-text
  {:color       colors/black
   :line-height 22
   :font-weight "500"
   :font-size   15})

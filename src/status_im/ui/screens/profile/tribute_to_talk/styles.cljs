(ns status-im.ui.screens.profile.tribute-to-talk.styles
  (:require [status-im.ui.components.colors :as colors]))

(def intro-container
  {:flex              1
   :align-items       :center
   :margin-horizontal 32})

(def intro-image
  {:width           246
   :height          219
   :margin-vertical 32})

(def intro-text
  {:typography :header
   :text-align :center})

(def description-label
  {:text-align :center
   :color      colors/gray})

(def intro-button
  {:margin-vertical    8
   :padding-horizontal 32
   :align-self         :center
   :justify-content    :center
   :align-items        :center})

(def bottom-toolbar
  {:height           60
   :border-top-width 1
   :border-top-color colors/gray-lighter})

(def step-n
  {:margin-top 5
   :font-size  14
   :text-align :center
   :color      colors/gray})

(def tribute-to-talk
  {:typography :main-medium
   :text-align :center})

(def container
  {:flex             1
   :background-color colors/white})

(def set-snt-amount-container
  {:flex-grow        1
   :flex-direction   :column
   :background-color colors/white})

(defn horizontal-separator
  [min-height max-height]
  {:flex       1
   :min-height min-height
   :max-height max-height})

(def number-row
  {:flex-direction :row})

(def number-pad
  {:flex          1
   :align-items   :center
   :margin-bottom 24
   :min-height    292
   :max-height    328})

(def number-container
  {:width            64
   :height           64
   :border-radius    32
   :justify-content  :center
   :align-items      :center
   :background-color colors/blue-light})

(def vertical-number-separator
  {:flex      1
   :min-width 16
   :max-width 32})

(def number
  {:font-size   22
   :color       colors/blue})

(def snt-amount-container
  {:margin-horizontal 16
   :justify-content   :center
   :align-items       :center})

(def snt-amount
  {:font-size 32})

(def snt-amount-label
  (assoc snt-amount :color colors/gray))

(def snt-asset-value
  {:typography :main-medium
   :color      colors/gray})

(def personalized-message-container
  {:flex-grow         1
   :align-items       :center
   :margin-horizontal 16
   :justify-content   :flex-start})

(def personalized-message-title
  {:margin-top    24
   :margin-bottom 10
   :align-self    :flex-start})

(def personalized-message-input
  {:border-radius       8
   :background-color    colors/gray-lighter
   :text-align-vertical :top
   :padding-horizontal  16
   :padding-vertical    16})

(def edit-view-message-container
  {:border-radius       8
   :background-color    colors/blue-light
   :margin-horizontal   72
   :padding-horizontal  12
   :padding-vertical    8})

(def finish-label
  {:typography :header
   :text-align :center})

(defn finish-circle [color radius]
  {:background-color color
   :width            (* 2 radius)
   :height           (* 2 radius)
   :border-radius    radius
   :justify-content  :center
   :align-items      :center})

(def finish-circle-with-shadow
  (assoc (finish-circle colors/white 40)
         :elevation     5
         :shadow-offset {:width 0 :height 2}
         :shadow-radius 4
         :shadow-color  (colors/alpha "#435971" 0.124066)))

(defn icon-view [color]
  {:background-color color
   :justify-content  :center
   :align-items      :center
   :border-radius    20
   :width            40
   :height           40})

(def current-snt-amount
  {:font-size   28
   :font-weight "500"})

(def edit-label
  {:color colors/blue})

(def edit-note
  {:color             colors/gray
   :margin-top        16
   :margin-horizontal 16
   :text-align        :center})

(def enabled-note
  {:border-radius     8
   :border-width      1
   :padding           12
   :border-color      colors/gray-lighter
   :margin-horizontal 16
   :margin-bottom     16})

(def learn-more-container
  {:flex-grow       1
   :justify-content :flex-start})

(def learn-more-link
  {:height             52
   :padding-horizontal 32
   :margin-bottom      16
   :align-items        :center
   :justify-content    :center})

(def learn-more-link-text
  {:color colors/blue})

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
  {:typography :title})

(def learn-more-section
  {:border-radius     12
   :border-width      1
   :margin-horizontal 32
   :padding           8
   :width             238
   :border-color      colors/gray-lighter})

(defn chat-bubble [tribute-sender?]
  {:background-color   (if tribute-sender? colors/blue-light colors/blue)
   :padding-horizontal 12
   :padding-vertical   6
   :margin-top         4
   :border-radius      8})

(def pay-to-chat-bubble
  {:background-color   colors/blue-light
   :padding-horizontal 12
   :padding-vertical   8
   :margin-top         4
   :border-radius      8})

(def pay-to-chat-container
  {:justify-content :center
   :align-items     :center
   :flex-direction  :row
   :padding-top     12
   :padding-bottom  4})

(def pay-to-chat-text
  {:color      colors/blue})

(defn payment-status-icon [pending?]
  {:width 24
   :height 24
   :border-radius 12
   :justify-content :center
   :align-items :center
   :background-color (if pending?
                       (colors/alpha colors/black 0.1)
                       colors/green)})

(def payment-status-text
  {:font-size 15
   :color colors/gray
   :margin-left 6
   :line-height 22})

(def edit-container
  {:justify-content :space-between
   :flex-grow       1})

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
  {:typography  :title
   :color       colors/red
   :margin-left 16})

(def remove-note
  {:margin-horizontal 16
   :color             colors/gray
   :text-align        :center
   :margin-top        12})

(def enabled-note-text
  {:typography :main-medium})

(def separator-style
  {:height           1
   :background-color colors/gray-lighter
   :align-self       :stretch})

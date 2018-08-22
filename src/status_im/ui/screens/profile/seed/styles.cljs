(ns status-im.ui.screens.profile.seed.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as common.styles]))

(def intro-container
  {:flex              1
   :align-items       :center
   :justify-content   :center
   :margin-horizontal 26})

(def intro-image
  {:padding-bottom 10
   :align-items    :center})

(def intro-text
  {:text-align     :center
   :font-size      22
   :font-weight    :bold
   :line-height    28
   :letter-spacing -0.3})

(def intro-description
  {:margin-top     8
   :line-height    21
   :text-align     :center
   :color          colors/gray})

(def intro-button
  {:flex-direction :row
   :margin-top     16})

(def six-words-container
  {:flex    1
   :padding 15})

(def six-word-row
  {:flex-direction :row})

(def six-word-num
  {:width          20
   :text-align     :right
   :opacity        0.4
   :font-size      15
   :letter-spacing -0.2})

(def six-words-word
  {:margin-left    24
   :font-size      15
   :letter-spacing -0.2})

(def six-words-separator
  {:height 12})

(def twelve-words-container
  {:flex    1
   :padding 16})

(def twelve-words-label
  {:font-size      14
   :line-height    21
   :letter-spacing -0.2})

(def twelve-words-description
  {:font-size      14
   :line-height    21
   :letter-spacing -0.2})

(def twelve-words-spacer
  {:flex 1})

(def twelve-words-button-container
  {:align-items :flex-end})

(def twelve-words-columns
  {:margin-top       8
   :margin-bottom    16
   :flex-direction   :row
   :border-radius    8
   :background-color colors/white
   :border-width     1
   :border-color     colors/gray-border})

(def twelve-words-columns-separator
  {:width            1
   :background-color colors/gray-border})

(def enter-word-container
  {:flex    1
   :padding 16})

(def enter-word-row
  {:flex-direction :row})

(def enter-word-label
  {:font-size      14
   :line-height    21
   :letter-spacing -0.2})

(def enter-word-n
  {:margin-left    8
   :font-size      14
   :line-height    21
   :letter-spacing -0.2
   :color          colors/gray})

(def enter-word-n-description
  {:font-size      14
   :line-height    21
   :letter-spacing -0.2
   :color          colors/gray})

(def finish-container
  {:flex               1
   :padding-horizontal 24
   :align-items        :center})

(def finish-logo-container
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def ok-icon
  {:color  :white
   :width  41
   :height 41})

(def finish-label
  {:font-size      22
   :font-weight    :bold
   :line-height    30
   :letter-spacing -0.3
   :text-align     :center})

(def finish-description
  {:margin-top  8
   :font-size   14
   :line-height 20
   :text-align  :center
   :color       colors/gray})

(def finish-button
  {:flex-direction :row
   :margin-top     16
   :margin-bottom  32})

(def backup-seed
  {:font-weight    :bold
   :font-size      15
   :letter-spacing -0.2
   :text-align     :center})

(def step-n
  {:margin-top 5
   :font-size  14
   :text-align :center
   :color      colors/gray})

(def backup-seed-container
  (merge
   common.styles/flex
   {:background-color colors/white}))

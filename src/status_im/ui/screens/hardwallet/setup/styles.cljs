(ns status-im.ui.screens.hardwallet.setup.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def container
  {:flex             1
   :justify-content :space-between
   :background-color colors/white})

(def inner-container
  {:flex-direction  :column
   :align-items     :center
   :justify-content :space-between})

;; setup step

(def maintain-card-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-top      41
   :width           "90%"
   :height          60
   :border-radius   10
   :border-width    1
   :border-color    colors/blue-light})

(def maintain-card-text
  {:typography         :caption
   :flex               1
   :padding-horizontal 20
   :color              colors/blue})

(def setup-steps-container
  {:flex-direction   :row
   :align-items      :baseline
   :flex             1
   :width            "95%"
   :background-color :white
   :padding-left     10
   :margin-right     10})

(def maintain-card-current-step-text
  {:typography :caption
   :color      colors/blue})

(def maintain-card-second-step-text
  {:typography   :caption
   :padding-left 8
   :color        colors/gray})

(def maintain-card-third-step-text
  {:typography   :caption
   :padding-left 8
   :color        colors/gray})

(def hardwallet-icon-container
  {:margin-left    20
   :flex-direction :row
   :align-items    :center})

(defn hardwallet-icon-indicator-small-container [opacity]
  {:margin-left 4
   :opacity     opacity})

(defn hardwallet-icon-indicator-middle-container [opacity]
  {:margin-left 1
   :opacity     opacity})

(defn hardwallet-icon-indicator-big-container [opacity]
  {:opacity opacity})

(def hardwallet-card-image-container
  {:margin-top  47
   :flex        1
   :align-items :center})

(def hardwallet-card-image
  {:width  255
   :height 160})

(def loading-view-container
  {:flex           1
   :flex-direction :column
   :align-items    :center
   :margin-top     100})

(def card-with-button-view-container
  {:flex           1
   :flex-direction :column
   :align-items    :center})

(def card-blank-container
  {:flex           1
   :flex-direction :column})

(def enter-pair-code-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :margin-top      80})

(def center-text-container
  {:margin-top 37})

(def center-text
  {:color      colors/gray
   :text-align :center})

(def center-title-text
  {:typography :header
   :text-align :center})

(def bottom-container
  {:height           52
   :width            "100%"
   :justify-content  :center
   :align-items      :center
   :border-top-width 1
   :border-color     colors/black-transparent})

(def bottom-button-container
  {:background-color colors/blue-light
   :align-items      :center
   :justify-content  :center
   :flex-direction   :row
   :width            160
   :height           44
   :border-radius    10})

(def begin-button-container
  {:background-color colors/blue-light
   :align-items      :center
   :justify-content  :center
   :flex-direction   :row
   :width            160
   :height           42
   :border-radius    10
   :margin-bottom    1})

(def bottom-button-text
  {:typography :main-medium
   :color      colors/blue})

(def next-button-container
  {:flex-direction   :row
   :justify-content  :space-between
   :align-items      :center
   :width            "100%"
   :height           52
   :border-top-width 1
   :border-color     colors/black-transparent})

(def back-and-next-buttons-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-vertical 15})

;; prepare step

(def center-container
  {:flex-direction :column
   :align-items    :center
   :height         200})

(def wizard-step-text
  {:typography     :caption
   :color          colors/blue
   :text-align     :center
   :padding-bottom 18})

(def generating-codes-for-pairing-text
  {:padding-top 8
   :width       314
   :text-align  :center
   :color       colors/gray})

(def estimated-time-text
  (assoc generating-codes-for-pairing-text :padding-top 25))

(def recovery-phrase-inner-container
  {:align-self :center})

(def check-recovery-phrase-text
  {:typography :header
   :text-align :center
   :color      colors/gray})

(def recovery-phrase-word-n-text
  {:typography :header
   :text-align :center})

(def recovery-phrase-description
  {:padding 16})

(def waiting-indicator-container
  {:height 200})

(def progress-bar-container
  {:width "65%"})

;; secret keys step

(def secret-keys-container
  {:flex            1
   :flex-direction  :column
   :justify-content :space-between
   :margin-top      30})

(def secret-keys-inner-container
  {:flex-direction  :column
   :justify-content :space-between
   :align-items     :center})

(def secret-keys-title-container
  {:width           292
   :margin-vertical 25})

(def secret-keys-title-text
  {:typography :header
   :text-align :center})

(def secret-keys-image-container
  {:width  120
   :height 120})

(def puk-code-title-text
  {:typography  :title-bold
   :padding-top 12})

(def secret-code-explanation-container
  {:margin-top    5
   :margin-bottom 15})

(def puk-code-explanation-text
  {:padding-horizontal 32
   :text-align         :center
   :padding-top        5
   :padding-bottom     10
   :color              colors/gray})

(def puk-code-numbers-container
  {:justify-content :center
   :flex-direction  :row})

(def puk-code-numbers-border-container
  {:border-bottom-width 2
   :width               302
   :text-align          :center
   :border-color        colors/gray-lighter})

(defstyle puk-code-numbers-inner-container
  {:width           "85%"
   :android         {:margin-horizontal 16}
   :height          94
   :align-items     :center
   :justify-content :space-between
   :flex-direction  :column
   :border-width    2
   :border-color    colors/gray-lighter
   :border-radius   10})

(def puk-code-text
  {:typography     :title-bold
   :padding-bottom 10
   :text-align     :center
   :color          colors/green})

;; card ready

(def card-ready-container secret-keys-container)

(def card-ready-inner-container
  {:align-self      :center
   :flex            1
   :justify-content :space-between})

;; enter pair code

(def enter-pair-code-title-container
  {:flex-direction :column
   :align-items    :center})

(defn enter-pair-code-input-container [width]
  {:width      (* width 0.9)
   :margin-top 10})

(def enter-pair-code-title-text
  {:typography :title
   :text-align :center})

(def enter-pair-code-explanation-text
  {:padding-top        5
   :text-align         :center
   :padding-horizontal 60
   :color              colors/gray})

(def card-is-empty-text
  {:typography    :title-bold
   :margin-bottom 20})

(def card-is-empty-prepare-text
  {:margin-top         25
   :padding-horizontal 40})

(def remaining-steps-container
  {:margin-top     55
   :margin-left    16
   :flex           1
   :width          "90%"
   :flex-direction :column})

(def remaining-steps-text
  {:color colors/gray})

(def remaining-step-row
  {:flex-direction :row
   :margin-top     15})

(def remaining-step-row-text
  {:border-width    1
   :border-radius   16
   :border-color    colors/black-transparent
   :align-items     :center
   :justify-content :center
   :width           32
   :height          32})

(def remaining-step-row-text2
  {:align-items     :center
   :justify-content :center
   :margin-left     11})

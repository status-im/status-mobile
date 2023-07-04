(ns status-im.ui.screens.wallet-connect.session-proposal.styles
  (:require [quo.design-system.colors :as colors]))

(defn toolbar-container
  [background-color]
  {:height           36
   :border-radius    18
   :background-color background-color
   :align-items      :center
   :flex-direction   :row
   :padding-left     13
   :padding-right    6})

(def toolbar-text
  {:flex-grow    1
   :margin-right 3})

(defn dapp-logo
  []
  {:width         120
   :height        120
   :resize-mode   :cover
   :margin-top    31
   :border-radius 16
   :border-width  2
   :border-color  (:interactive-02 @colors/theme)
   :padding       5})

(def sheet-body-container
  {:flex        1
   :align-items :center})

(defn acc-sheet
  []
  {:background-color        (:ui-background @colors/theme)
   :border-top-right-radius 16
   :border-top-left-radius  16
   :padding-bottom          1})

(defn proposal-sheet-container
  []
  {:background-color        (:ui-background @colors/theme)
   :width                   "100%"
   :align-items             :center
   :padding-top             0
   :padding-bottom          50
   :border-top-right-radius 16
   :border-top-left-radius  16})

(defn proposal-sheet-header
  []
  {:flex-direction      :row
   :align-items         :center
   :justify-content     :center
   :height              56
   :width               "100%"
   :border-color        colors/gray-lighter
   :border-bottom-width 1})

(def proposal-title-container
  {:align-items :center
   :margin-top  21})

(def message-title
  {:margin-top        10
   :margin-bottom     14
   :margin-horizontal 72.5
   :text-align        :center})

(defn proposal-buttons-container
  []
  {:width              "100%"
   :height             76
   :border-color       colors/gray-lighter
   :border-top-width   1
   :flex               1
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 16})

(defn success-button-container
  []
  {:width              "100%"
   :height             76
   :border-color       colors/gray-lighter
   :border-top-width   1
   :flex-direction     :row
   :justify-content    :flex-end
   :align-items        :center
   :padding-horizontal 16})

(defn account-container
  [color account-selected?]
  {:height             34
   :background-color   color
   :border-radius      17
   :padding-horizontal 10
   :justify-content    :center
   :margin-right       4
   :opacity            (if account-selected? 1 0.5)})

(def account-selector-container
  {:height             80
   :width              "100%"
   :justify-content    :center
   :padding-horizontal 16})

(def account-selector-wrapper
  {:margin-top 40
   :width      "100%"})

(def account-selector-list
  {:height     40
   :width      "100%"
   :margin-top 10})

(def single-account-container
  {:width       "100%"
   :align-items :center
   :padding-top 8})

(defn blur-view
  []
  {:position         :absolute
   :top              60
   :left             0
   :right            0
   :bottom           0
   :background-color (:blurred-bg @colors/theme)})

(def shadow
  {:width   "100%"
   :height  50
   :opacity 0.3})

(defn management-sheet-header
  []
  {:width               "100%"
   :flex-direction      :row
   :padding             16
   :align-items         :center
   :border-bottom-width 1
   :border-bottom-color colors/gray-lighter})

(def management-icon
  {:width         40
   :height        40
   :border-radius 20
   :margin-right  16})

(def app-info-container
  {:flex-direction :column
   :flex           1})

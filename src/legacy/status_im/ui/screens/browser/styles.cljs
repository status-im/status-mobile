(ns legacy.status-im.ui.screens.browser.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def browser {:flex 1})

(defn navbar
  []
  {:background-color   colors/white
   :height             100
   :flex-direction     :row
   :align-items        :center
   :justify-content    :space-between
   :border-top-color   colors/gray-lighter
   :border-top-width   1
   :padding-horizontal 24
   :padding-bottom     50})

(def disabled-button
  {:opacity 0.4})

(def web-view-error
  {:justify-content  :center
   :align-items      :center
   :position         :absolute
   :padding          20
   :top              0
   :left             0
   :bottom           0
   :right            0
   :background-color colors/white})

(def web-view-error-text
  {:color       colors/gray
   :line-height 22
   :text-align  :center})

(defn toolbar-content
  []
  {:flex-direction     :row
   :flex               1
   :border-radius      8
   :max-height         36
   :background-color   colors/gray-lighter
   :padding-horizontal 10
   :align-items        :center
   :align-self         :center
   :margin-horizontal  16
   :margin-vertical    10})

(def url-input
  {:flex                1
   :text-align-vertical :center
   :margin              0
   :padding             0
   :margin-left         6})

(def url-text-container
  {:justify-content   :center
   :flex              1
   :margin-horizontal 5})

(def dot
  {:height           4
   :width            4
   :background-color colors/blue
   :border-radius    2})

(def permissions-panel-container
  {:position :absolute
   :top      0
   :bottom   0
   :right    0
   :left     0})

(defn permissions-panel-background
  [alpha-value]
  {:flex             1
   :background-color colors/black
   :opacity          alpha-value})

(def panel-height 354)

(defn permissions-panel
  [bottom-anim-value]
  {:height                  panel-height
   :position                :absolute
   :transform               [{:translateY bottom-anim-value}]
   :right                   0
   :left                    0
   :bottom                  0
   :align-items             :center
   :background-color        colors/white
   :border-top-left-radius  8
   :border-top-right-radius 8})

(def permissions-panel-icons-container
  {:margin-top      26
   :align-items     :center
   :justify-content :center
   :flex-direction  :row})

(def permissions-panel-dapp-icon-container
  {:height           40
   :width            40
   :background-color colors/gray-lighter
   :border-radius    20
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-ok-icon-container
  {:height           24
   :width            24
   :background-color colors/blue-light
   :border-radius    12
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-ok-ico
  {:color  colors/blue
   :width  16
   :height 16})

(def blocked-access-container
  {:align-items :center
   :margin      16})

(def blocked-access-icon-container
  {:height           40
   :width            40
   :background-color colors/blue-light
   :border-radius    20
   :align-items      :center
   :justify-content  :center})

(def blocked-access-camera-icon
  {:color  colors/blue
   :width  20
   :height 20})

(def blocked-access-text-container
  {:margin-top 16})

(def blocked-access-text
  {:text-align :center})

(def blocked-access-buttons-container
  {:flex-direction  :row
   :justify-content :center
   :margin-top      16})

(def blocked-access-button-wrapper
  {:flex              1
   :margin-horizontal 8})

(def blocked-access-button
  {:margin-horizontal 8})

(def permissions-panel-wallet-icon-container
  {:height           40
   :width            40
   :background-color colors/blue
   :border-radius    20
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-title-label
  {:typography        :title-bold
   :margin-horizontal 20
   :text-align        :center
   :margin-top        16})

(def permissions-panel-description-label
  {:margin-horizontal 20
   :color             colors/gray
   :text-align        :center
   :margin-top        16})

(def permissions-account
  {:flex-direction     :row
   :border-radius      36
   :border-width       1
   :border-color       colors/gray-lighter
   :padding-horizontal 8
   :padding-vertical   6
   :margin-top         16
   :align-items        :center
   :justify-content    :center})

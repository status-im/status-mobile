(ns status-im.ui.screens.browser.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def browser {:flex 1})

(styles/def dapp-name
  {:flex            1
   :justify-content :center
   :margin-left     12
   :android         {:padding-bottom 6}})

(def dapp-name-text
  {:font-size 16})

(styles/def dapp-text
  {:color   colors/gray
   :ios     {:margin-top 4}
   :android {:font-size 13}})

(def navbar
  {:background-color   :white
   :height             48
   :flex-direction     :row
   :align-items        :center
   :justify-content    :space-between
   :padding-horizontal 32})

(def disabled-button
  {:opacity 0.4})

(def forward-button
  {:margin-left 72})

(def share-button
  {:margin-right 72})

(def background
  {:flex             1
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

(def web-view-error
  {:flex             1
   :justify-content  :center
   :align-items      :center
   :background-color colors/gray-lighter})

(def web-view-error-text
  {:color colors/gray})

(def toolbar-content
  {:flex-direction     :row
   :flex               1
   :border-radius      8
   :max-height         36
   :background-color   colors/gray-lighter
   :padding-horizontal 12
   :margin-right       8
   :align-items        :center
   :align-self         :center
   :margin-top         10
   :margin-left        56})

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
   :background-color "#E4E6EB"
   :border-radius    2})

(def permissions-panel-container
  {:position :absolute
   :top      0
   :bottom   0
   :right    0
   :left     0})

(defn permissions-panel-background [alpha-value]
  {:flex             1
   :background-color colors/black
   :opacity          alpha-value})

(def panel-height 354)

(defn permissions-panel [bottom-anim-value]
  {:height                  panel-height
   :position                :absolute
   :transform               [{:translateY bottom-anim-value}]
   :right                   0
   :left                    0
   :bottom                  0
   :align-items             :center
   :background-color        :white
   :border-top-left-radius  8
   :border-top-right-radius 8})

(def permissions-panel-icons-container
  {:margin-top      26
   :align-items     :center
   :justify-content :center
   :flex-direction  :row})

(def permissions-panel-dapp-icon-container
  {:height           48
   :width            48
   :background-color "#E4E6EB"
   :border-radius    24
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-ok-icon-container
  {:height           24
   :width            24
   :background-color "#48EA77"
   :border-radius    12
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-ok-ico
  {:color  :white
   :width  12
   :height 12})

(def permissions-panel-wallet-icon-container
  {:height           48
   :width            48
   :background-color colors/blue
   :border-radius    24
   :align-items      :center
   :justify-content  :center})

(def permissions-panel-title-label
  {:typography        :header
   :margin-horizontal 20
   :text-align        :center
   :margin-top        19})

(def permissions-panel-description-label
  {:margin-horizontal 20
   :color             colors/gray
   :text-align        :center
   :margin-top        9})

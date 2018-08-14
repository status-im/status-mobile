(ns status-im.ui.screens.browser.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def browser {:flex 1})

(defstyle dapp-name
  {:flex            1
   :justify-content :center
   :margin-left     12
   :android         {:padding-bottom 6}})

(def dapp-name-text
  {:color     colors/text-light-gray
   :font-size 16})

(defstyle dapp-text
  {:color   colors/gray
   :ios     {:margin-top 4}
   :android {:font-size 13}})

(def toolbar
  {:background-color   :white
   :height             48
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 32})

(def disabled-button
  {:opacity 0.4})

(def forward-button
  {:margin-left 72})

(def background
  {:flex             1
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

(def web-view-loading
  {:flex             1
   :background-color colors/gray-transparent
   :align-items      :center
   :justify-content  :center
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0})

(def web-view-error
  {:flex             1
   :justify-content  :center
   :align-items      :center
   :background-color colors/gray-lighter})

(def web-view-error-text
  {:color colors/gray})

(defnstyle toolbar-content [show-actions]
  {:flex-direction     :row
   :flex               1
   :border-radius      4
   :height             36
   :background-color   colors/gray-lighter
   :padding-horizontal 12
   :margin-right       5
   :align-items        :center
   :android            {:margin-left (if show-actions 66 20)}
   :ios                {:margin-left 20}})

(defstyle url-input
  {:flex              1
   :font-size         14
   :letter-spacing    -0.2
   :margin-horizontal 5
   :android           {:padding 0}})

(def url-text
  {:font-size         14
   :letter-spacing    -0.2
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

(defn permissions-panel [bottom-anim-value]
  {:height                  354
   :position                :absolute
   :bottom                  bottom-anim-value
   :right                   0
   :left                    0
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

(def permissions-panel-d-label
  {:font-size   22
   :color       colors/gray
   :font-weight :bold})

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
  {:margin-horizontal 20
   :font-size         22
   :line-height       28
   :text-align        :center
   :margin-top        19
   :font-weight       :bold})

(def permissions-panel-description-label
  {:margin-horizontal 20
   :color             colors/gray
   :font-size         15
   :line-height       22
   :text-align        :center
   :margin-top        9})

(def permissions-panel-permissions-label
  {:color       colors/blue
   :font-size   14
   :margin-left 10})
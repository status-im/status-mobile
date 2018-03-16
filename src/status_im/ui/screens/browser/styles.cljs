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
   :ios     {:font-size  14
             :margin-top 4}
   :android {:font-size 13}})

(def toolbar
  {:background-color   :white
   :height             48
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 32})

(def forward-button
  {:margin-left 72})

(def background
  {:flex             1
   :background-color colors/gray-lighter
   :align-items      :center
   :justify-content  :center})

(def web-view-loading
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def web-view-error
  {:justify-content :center
   :align-items     :center
   :flex-direction  :row})

(defnstyle toolbar-content [show-actions]
  {:flex-direction     :row
   :flex               1
   :border-radius      4
   :height             36
   :background-color   colors/gray-lighter
   :padding-horizontal 12
   :android            {:align-items    :flex-start
                        :margin-left    (if show-actions 66 20)
                        :padding-bottom 6}
   :ios                {:align-items       :center
                        :margin-horizontal 15}})

(defstyle url-input
  {:flex           1
   :font-size      14
   :letter-spacing -0.2
   :android        {:padding 0}})

(def toolbar-content-dapp
  {:flex-direction    :row
   :margin-horizontal 15})
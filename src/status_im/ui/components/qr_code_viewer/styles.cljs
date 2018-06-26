(ns status-im.ui.components.qr-code-viewer.styles
  (:require [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def qr-code-hint
  {:color          colors/gray
   :padding-top    24
   :padding-bottom 22
   :text-align     :center})

(def qr-code-padding
  15)

(defn qr-code-container [width]
  {:background-color colors/white
   :width            width
   :align-items      :center
   :justify-content  :center
   :padding          qr-code-padding
   :border-radius    8})

(defstyle name-container
  {:flex           0.6
   :flex-direction :column
   :ios            {:align-items :center}
   :android        {:margin-left 15}})

(defstyle name-text
  {:color     colors/black
   :font-size 17
   :ios       {:letter-spacing -0.2}})

(def address-text
  {:color     colors/white
   :font-size 12})

(def toolbar-action-container
  {:flex            0.2
   :flex-direction  :column
   :align-items     :center
   :justify-content :center})

(def toolbar-action-icon-container
  {:width           40
   :height          40
   :align-items     :center
   :justify-content :center})

(def wallet-qr-code
  {:flex-grow      1
   :flex-direction :column})

(def account-toolbar
  {:background-color colors/white})

(def toolbar-contents
  {:flex-grow       1
   :flex-direction  :row
   :height          55
   :align-items     :center
   :justify-content :center})

(def qr-code
  {:flex-grow        1
   :align-items      :center
   :justify-content  :center})

(def footer
  {:flex-direction   :row
   :justify-content  :center
   :padding-top      17})

(def wallet-info
  {:flex-grow      1
   :align-items    :center
   :padding-bottom 20})

(def hash-value-type
  {:color          colors/black
   :padding-bottom 5})

(def hash-value-text
  {:color             colors/black
   :align-self        :stretch
   :margin-horizontal 60
   :text-align        :center
   :font-size         15
   :letter-spacing    -0.2
   :line-height       20})

(def done-button-text
  {:color colors/white})

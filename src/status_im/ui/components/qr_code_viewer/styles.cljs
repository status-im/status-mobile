(ns status-im.ui.components.qr-code-viewer.styles
  (:require [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def qr-code-hint
  {:color           colors/gray
   :margin-vertical 12
   :text-align      :center})

(def qr-code-max-width 208)
(def qr-code-padding 16)

(defn qr-code-container [width]
  {:align-self       :center
   :width            (+ width qr-code-padding qr-code-padding)
   :background-color colors/white
   :border-color     colors/gray-light
   :border-width     1
   :border-radius    8
   :padding          qr-code-padding})

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

(def wallet-qr-code
  {:flex-grow      1
   :flex-direction :column})

(def account-toolbar
  {:background-color colors/white})

(def footer
  {:flex-direction   :row
   :justify-content  :center})

(def wallet-info
  {:flex-grow      1
   :align-items    :center})

(def hash-value-type
  {:color          colors/black
   :padding-bottom 5})

(def hash-value-text
  {:color              colors/black
   :align-self         :stretch
   :border-color       colors/gray-light
   :border-width       1
   :margin-horizontal  16
   :padding-horizontal 8
   :padding-vertical   6
   :border-radius      8
   :text-align         :center
   :font-size          15
   :letter-spacing     -0.2
   :line-height        20})

(def done-button-text
  {:color colors/white})

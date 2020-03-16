(ns status-im.ui.components.qr-code-viewer.styles
  (:require [status-im.ui.components.colors :as colors]))

(def qr-code-hint
  {:color           colors/gray
   :margin-vertical 12
   :text-align      :center})

(def qr-code-max-width 208)
(def qr-code-padding 16)

(defn qr-code-container [width]
  {:align-self       :center
   :width            width
   :height           width
   :padding-horizontal 16
   :background-color colors/white-persist
   :border-color     colors/black-transparent
   :align-items      :center
   :justify-content  :center
   :border-width     1
   :border-radius    8})

(def name-text
  {:font-size 17})

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

(def hash-value-text
  {:align-self         :stretch
   :border-color       colors/black-transparent
   :border-width       1
   :margin-horizontal  16
   :padding-horizontal 8
   :padding-vertical   6
   :border-radius      8
   :text-align         :center})

(def done-button-text
  {:color colors/white})

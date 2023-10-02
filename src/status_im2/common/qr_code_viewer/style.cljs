(ns status-im2.common.qr-code-viewer.style
  (:require [quo2.foundations.colors :as colors]))

(def qr-code-padding 16)

(defn qr-code-container
  [width]
  {:align-self         :center
   :width              width
   :height             width
   :padding-horizontal 16
   :background-color   colors/white
   :align-items        :center
   :justify-content    :center
   :border-radius      8})

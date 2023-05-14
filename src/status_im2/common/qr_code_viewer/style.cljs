(ns status-im2.common.qr-code-viewer.style
  (:require [quo.design-system.colors :as colors]))

(def qr-code-padding 16)

(defn qr-code-container
  [width]
  {:align-self         :center
   :width              width
   :height             width
   :padding-horizontal 16
   :background-color   colors/white-persist
   :border-color       colors/black-transparent
   :align-items        :center
   :justify-content    :center
   :border-width       1
   :border-radius      8})

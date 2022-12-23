(ns status-im2.common.not-implemented
  (:require [react-native.core :as rn]))

(defn not-implemented [content]
  [rn/view {:border-color :red :border-width 1}
   content])

(ns status-im.components.share)

(def class (js/require "react-native-share"))

(defn open [opts]
  (.open class (clj->js opts)))

(ns status-im.components.share
  (:require [status-im.utils.platform :as p]))

(def class (js/require "react-native-share"))

(defn open [opts]
  (.open class (clj->js opts)))
(ns status-im.utils.fs
  (:require [clojure.string :as s]
            [status-im.utils.utils :as u]))

(def fs (u/require "react-native-fs"))

(defn move-file [src dst handler]
  (let [result (.moveFile fs src dst)
        result (.then result #(handler nil %))
        result (.catch result #(handler % nil))]
    result))
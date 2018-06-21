(ns status-im.utils.fs
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn move-file [src dst handler]
  (-> (.moveFile rn-dependencies/fs src dst)
      (.then #(handler nil %))
      (.catch #(handler % nil))))

(defn read-file [path encoding on-read on-error]
  (-> (.readFile rn-dependencies/fs path encoding)
      (.then on-read)
      (.catch on-error)))

(defn read-dir [path]
  (.readDir rn-dependencies/fs path))

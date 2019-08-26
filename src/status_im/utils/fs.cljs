(ns status-im.utils.fs
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn move-file [src dst]
  (.moveFile rn-dependencies/fs src dst))

(defn read-file [path encoding on-read on-error]
  (-> (.readFile rn-dependencies/fs path encoding)
      (.then on-read)
      (.catch on-error)))

(defn read-dir [path]
  (.readDir rn-dependencies/fs path))

(defn mkdir [path]
  (.mkdir rn-dependencies/fs path))

(defn unlink [path]
  (.unlink rn-dependencies/fs path))

(defn file-exists? [path]
  (.exists rn-dependencies/fs path))

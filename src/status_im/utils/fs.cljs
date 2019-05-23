(ns status-im.utils.fs
  (:require [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn move-file [src dst]
  (.moveFile ^js (rn-dependencies/fs) src dst))

(defn read-file [path encoding on-read on-error]
  (-> ^js (.readFile (rn-dependencies/fs) path encoding)
      (.then on-read)
      (.catch on-error)))

(defn read-dir [path]
  (.readDir ^js (rn-dependencies/fs) path))

(defn mkdir [path]
  (.mkdir ^js (rn-dependencies/fs) path))

(defn unlink [path]
  (.unlink ^js (rn-dependencies/fs) path))

(defn file-exists? [path]
  (.exists ^js (rn-dependencies/fs) path))

(ns status-im.utils.fs
  (:require ["react-native-fs" :as react-native-fs]))

(defn move-file [src dst]
  (.moveFile react-native-fs src dst))

(defn stat [path on-stat on-error]
  (-> (.stat react-native-fs path)
      (.then on-stat)
      (.catch on-error)))

(defn read-file [path encoding on-read on-error]
  (-> (.readFile react-native-fs path encoding)
      (.then on-read)
      (.catch on-error)))

(defn read-dir [path]
  (.readDir react-native-fs path))

(defn mkdir [path]
  (.mkdir react-native-fs path))

(defn unlink [path]
  (.unlink react-native-fs path))

(defn file-exists? [path]
  (.exists react-native-fs path))

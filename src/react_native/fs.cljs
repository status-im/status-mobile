(ns react-native.fs
  (:require ["react-native-fs" :as react-native-fs]))

(defn move-file
  [src dst]
  (.moveFile react-native-fs src dst))

(defn stat
  [path on-stat on-error]
  (-> (.stat react-native-fs path)
      (.then on-stat)
      (.catch on-error)))

(defn read-file
  [path encoding on-read on-error]
  (-> (.readFile react-native-fs path encoding)
      (.then on-read)
      (.catch on-error)))

(defn write-file
  [path content encoding on-write on-error]
  (-> (.writeFile react-native-fs path content encoding)
      (.then on-write)
      (.catch on-error)))

(defn read-dir
  [path]
  (.readDir react-native-fs path))

(defn mkdir
  [path]
  (.mkdir react-native-fs path))

(defn unlink
  [path]
  (.unlink react-native-fs path))

(defn file-exists?
  [path]
  (.exists react-native-fs path))

(defn cache-dir
  []
  (.-CachesDirectoryPath ^js react-native-fs))

(defn copy-assets
  [src dest]
  (.copyFileAssets ^js react-native-fs src dest))

(defn main-bundle-path
  []
  (.-MainBundlePath ^js react-native-fs))

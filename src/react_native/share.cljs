(ns react-native.share
  (:require
    ["react-native-share" :default react-native-share]))

(defn open
  ([options]
   (-> ^js react-native-share
       (.open (clj->js options)))))

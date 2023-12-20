(ns react-native.share
  (:require
    ["react-native-share" :default react-native-share]))

(defn open
  ([options]
   (open options #() #()))
  ([options on-success on-error]
   (-> ^js react-native-share
       (.open (clj->js options))
       (.then on-success)
       (.catch on-error))))

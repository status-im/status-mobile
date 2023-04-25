(ns react-native.safe-area
  (:require ["react-native-static-safe-area-insets" :default StaticSafeAreaInsets]
            [oops.core :as oops]
            [react-native.platform :as platform]
            [react-native.navigation :as navigation]))

(defn- get-static-top
  []
  (oops/oget StaticSafeAreaInsets "safeAreaInsetsTop"))

(defn- get-static-bottom
  []
  (oops/oget StaticSafeAreaInsets "safeAreaInsetsBottom"))

(defn get-top
  []
  (if platform/ios?
    (get-static-top)
    (navigation/status-bar-height)))

(defn get-bottom
  []
  (if platform/ios?
    (get-static-bottom)
    0))

(defn get-insets
  []
  {:top    (get-top)
   :bottom (get-bottom)})

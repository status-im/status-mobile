(ns react-native.safe-area
  (:require [react-native.platform :as platform]
            [react-native.safe-area-context :as safe-area-context]))

(def ^:private safe-area-top-fix
  "Subtract to the top safe area to match a standard design.
  The values depend on our components padding (usually 12)"
  {:android {:default 0}
   :iphone  {62       11 ;; 16 +
             59       12 ;; 14Pro -> 15Pro
             :default 8}}) ;; 11 -> 14, 13mini

(def ^:private safe-area-bottom-fix
  "Subtract to the bottom safe area to match a standard design.
  The values depend on our components padding (usually 12)"
  {:android {:default 0}
   :iphone  {:default 12}})

(defn- get-top-fix
  [raw-top-safe-area]
  (let [platform (if platform/ios? :iphone :android)
        default  (-> safe-area-top-fix platform :default)]
    (get-in safe-area-top-fix [platform raw-top-safe-area] default)))

(defn- get-bottom-fix
  []
  (let [platform (if platform/ios? :iphone :android)]
    (-> safe-area-bottom-fix platform :default)))

(defn- get-top-for-views
  "Safe area top that must be used when creating screens"
  [original-top-safe-area]
  (- original-top-safe-area (get-top-fix original-top-safe-area)))

(defn- get-bottom-for-views
  "Safe area bottom that must be used when creating screens"
  [original-bottom-safe-area]
  (if platform/ios?
    (- original-bottom-safe-area (get-bottom-fix))
    0))

(def top (-> safe-area-context/initial-window-metrics :insets :top get-top-for-views))
(def bottom (-> safe-area-context/initial-window-metrics :insets :bottom get-bottom-for-views))
(def insets {:top top :bottom bottom})
(def window (:frame safe-area-context/initial-window-metrics))

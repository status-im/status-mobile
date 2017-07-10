(ns status-im.components.mapbox
  (:require [reagent.core :as r]
            [status-im.components.styles :as common]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view touchable-highlight text]]))

(def react-native-mapbox-gl (js/require "react-native-mapbox-gl"))

(defn get-property [name]
  (aget react-native-mapbox-gl name))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(.setAccessToken react-native-mapbox-gl "pk.eyJ1Ijoic3RhdHVzaW0iLCJhIjoiY2oydmtnZjRrMDA3czMzcW9kemR4N2lxayJ9.Rz8L6xdHBjfO8cR3CDf3Cw")

(def mapview (get-class "MapView"))

(ns status-im.ui.components.mapbox
  (:require [reagent.core :as r]
            [status-im.ui.components.styles :as common]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [view touchable-highlight text]]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn get-property [name]
  (aget rn-dependencies/mapbox-gl name))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(.setAccessToken rn-dependencies/mapbox-gl "pk.eyJ1Ijoic3RhdHVzaW0iLCJhIjoiY2oydmtnZjRrMDA3czMzcW9kemR4N2lxayJ9.Rz8L6xdHBjfO8cR3CDf3Cw")

(def mapview (get-class "MapView"))

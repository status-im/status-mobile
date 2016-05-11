(ns syng-im.components.realm
  (:require [reagent.core :as r]))

(set! js/window.RealmReactNative (js/require "realm/react-native"))

(def list-view-class (r/adapt-react-class (.-ListView js/RealmReactNative)))

(defn list-view [props]
  [list-view-class (merge {:enableEmptySections true} props)])

(comment


  ;(set! js/wat (js/require "realm.react-native.ListView"))
  ;(.-Results js/Realm)
  ;
  ;(r/realm)
  ;
  ;(require '[syng-im.persistence.realm :as r])

  )

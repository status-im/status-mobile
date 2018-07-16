(ns status-im.tron)

(def tron (.-default (js/require "reactotron-react-native")))
(.. tron
    configure
    useReactNative
    connect)

(defn log [message]
  (.log tron message))

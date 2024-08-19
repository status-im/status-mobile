(ns react-native.config
  (:require
    ["react-native-config" :default react-native-config]))

(def config (js->clj react-native-config :keywordize-keys true))

(defn get-config
  ([k] (get config k))
  ([k not-found] (get config k not-found)))

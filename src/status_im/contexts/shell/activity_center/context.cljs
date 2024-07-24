(ns status-im.contexts.shell.activity-center.context
  (:require
    ["react" :as react]
    [oops.core :as oops]
    [react-native.core :as rn]))

(defonce ^:private context
  (react/createContext {}))

(defn provider
  [state & children]
  (into [:> (oops/oget context :Provider) {:value state}]
        children))

(defn use-context
  []
  (let [ctx (rn/use-context context)]
    {:active-swipeable (oops/oget ctx :activeSwipeable)}))

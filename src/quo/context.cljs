(ns quo.context
  (:require
    ["react" :as react]
    [oops.core :as oops]
    [react-native.core :as rn]))

(defonce ^:private context (react/createContext nil))

(defn provider
  [data & children]
  (into [:> (.-Provider context) {:value #js {:cljData data}}]
        children))

(defn use-theme
  "A hook that returns the current theme keyword."
  []
  (if-let [data (rn/use-context context)]
    (:theme (oops/oget data :cljData))
    :light))

(defn use-screen-id
  "A hook that returns the current screen id."
  []
  (when-let [data (rn/use-context context)]
    (:screen-id (oops/oget data :cljData))))

(defn use-screen-params
  "A hook that returns the current screen params"
  []
  (when-let [data (rn/use-context context)]
    (:screen-params (oops/oget data :cljData))))

(ns quo.theme
  (:require
    ["react" :as react]
    [react-native.core :as rn]))

(defonce ^:private theme-context (react/createContext :light))

(defn provider
  [theme & children]
  (into [:> (.-Provider theme-context) {:value theme}]
        children))

(defn use-theme
  "A hook that returns the current theme keyword."
  []
  (keyword (rn/use-context theme-context)))

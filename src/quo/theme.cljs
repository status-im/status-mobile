(ns quo.theme
  (:require
    ["react" :as react]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    utils.transforms))

(defonce ^:private theme-context (react/createContext :light))
(defonce ^:private theme-state (reagent/atom :light))

(defn dark?
  []
  (= :dark @theme-state))

(defn get-theme
  []
  (or @theme-state :dark))

(defn set-theme
  [value]
  (reset! theme-state value))

(defn theme-value
  "Returns a value based on the current/override-theme theme."
  ([light-value dark-value]
   (theme-value light-value dark-value nil))
  ([light-value dark-value override-theme]
   (let [theme (or override-theme (get-theme))]
     (if (= theme :light) light-value dark-value))))

(defn provider
  "Wrap `children` in a React Provider using `quo.theme/theme-context` as the
  context.

  `options`: Clojure map. Currently we only use the `:theme` key. In the future
  we may support other settings.
  "
  [options & children]
  (into [:> (.-Provider theme-context) {:value options}]
        children))

(defn use-theme
  "A hook that returns the current theme context."
  []
  (keyword (rn/use-context theme-context)))

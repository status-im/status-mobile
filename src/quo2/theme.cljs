(ns quo2.theme
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]
            utils.transforms))

(defonce ^:private theme-context (rn/create-context :light))
(defonce ^:private theme-state (reagent/atom :light))

(defn dark?
  []
  (= :dark @theme-state))

(defn get-theme
  []
  @theme-state)

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
  "Wrap `children` in a React Provider using `quo2.theme/theme-context` as the
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
  (utils.transforms/js->clj (rn/use-context theme-context)))

(defn ^:private f-with-theme
  [component props & args]
  (let [theme (-> (use-theme) :theme keyword)]
    (into [component (assoc props :theme theme)] args)))

(defn with-theme
  "Create a functional component that assoc `:theme` into the first arg of
  `component`. The theme value is taken from the nearest `quo2.theme/provider`."
  [component]
  (fn [& args]
    (into [:f> f-with-theme component] args)))

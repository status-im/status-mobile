(ns quo2.theme
  (:require [reagent.core :as reagent]))

(def theme (reagent/atom :light))

(defn dark?
  []
  (= :dark @theme))

(defn get-theme
  []
  @theme)

(defn set-theme
  [value]
  (reset! theme value))

(defn theme-value
  "Returns a value based on the current/override-theme theme."
  ([light-value dark-value]
   (theme-value light-value dark-value nil))
  ([light-value dark-value override-theme]
   (let [theme (or override-theme (get-theme))]
     (if (= theme :light) light-value dark-value))))

(ns quo2.theme
  (:require [reagent.core :as reagent]))

(def theme (reagent/atom :light))

(defn dark? []
  (= :dark @theme))

(defn get-theme []
  @theme)

(defn set-theme [value]
  (reset! theme value))
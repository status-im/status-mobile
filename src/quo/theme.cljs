(ns quo.theme
  (:require [quo.design-system.colors :as colors]
            [reagent.core :as reagent]))

(def theme (reagent/atom nil))

(defn dark? []
  (= :dark @theme))

(defn get-theme []
  @theme)

(defn set-theme [value]
  (reset! theme value)
  (reset! colors/theme (case value
                         :dark colors/dark-theme
                         colors/light-theme))
  (colors/set-legacy-theme-type value))

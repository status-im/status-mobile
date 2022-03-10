(ns quo.theme
  (:require [quo.design-system.colors :as colors]))

(def theme (atom nil))

(defn get-theme []
  @theme)

(defn set-theme [value]
  (reset! theme value)
  (reset! colors/theme (case value
                         :dark colors/dark-theme
                         colors/light-theme))
  (colors/set-legacy-theme-type value))

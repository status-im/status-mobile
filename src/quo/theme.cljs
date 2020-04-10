(ns quo.theme
  (:require [quo.design-system.colors :as colors]))

(defn set-theme [theme]
  (reset! colors/theme (case theme
                         :dark colors/dark-theme
                         colors/light-theme)))

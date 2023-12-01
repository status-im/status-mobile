(ns status-im2.contexts.profile.settings.style
  (:require [quo.foundations.colors :as colors]))

(defn navigation-wrapper
  [{:keys [customization-color inset theme]}]
  {:padding-top      inset
   :background-color (colors/resolve-color customization-color theme 40)})

(def footer-container
  {:padding-horizontal 20
   :padding-vertical   12})

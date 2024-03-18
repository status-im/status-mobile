(ns status-im.contexts.profile.settings.style
  (:require [quo.foundations.colors :as colors]))

(defn navigation-wrapper
  [{:keys [customization-color inset theme]}]
  {:padding-top      inset
   :background-color (colors/resolve-color customization-color theme 40)})

(defn footer-container
  [bottom]
  {:padding-horizontal 20
   :padding-top        20
   :padding-bottom     (- 78 bottom)})

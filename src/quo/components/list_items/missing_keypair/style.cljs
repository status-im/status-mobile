(ns quo.components.list-items.missing-keypair.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:flex-direction   :row
   :align-items      :center
   :background-color colors/white-opa-5
   :flex             1
   :padding-right    12
   :padding-left     8
   :padding-vertical 8
   :border-radius    12})

(def icon-container
  {:border-radius 32
   :border-width  1
   :border-color  colors/white-opa-5})

(def name-container
  {:flex          1
   :padding-right 16
   :padding-left  8})

(def preview-list-container
  {:padding-right 16})

(defn options-icon-color
  [{:keys [theme blur?]}]
  (if blur?
    colors/white-opa-70
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

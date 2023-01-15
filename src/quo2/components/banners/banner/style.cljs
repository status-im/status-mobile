(ns quo2.components.banners.banner.style
  (:require [quo2.foundations.colors :as colors]))


(def container
  {:height           40
   :flex             1
   :background-color colors/primary-50-opa-20
   :flex-direction   :row
   :align-items      :center
   :padding-right    22
   :padding-left     20
   :padding-vertical 10})

(def counter
  {:flex            1
   :justify-content :center
   :align-items     :center})

(def icon
  {:flex         1
   :margin-right 10})

(defn text
  [hide-pin?]
  {:flex         (if hide-pin? 16 15)
   :margin-right 10})


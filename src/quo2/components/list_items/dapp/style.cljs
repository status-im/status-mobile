(ns quo2.components.list-items.dapp.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-background-color
  [{:keys [pressed? state blur? customization-color theme]}]
  (cond
    (and pressed? (= theme :dark) blur?)          colors/white-opa-5

    pressed?                                      (colors/theme-colors
                                                   (colors/custom-color customization-color 50 5)
                                                   (colors/custom-color customization-color 60 5)
                                                   theme)

    (and (= state :active) (= theme :dark) blur?) colors/white-opa-10

    (= state :active)                             (colors/theme-colors
                                                   (colors/custom-color customization-color 50 10)
                                                   (colors/custom-color customization-color 60 10)
                                                   theme)

    :else                                         :transparent))

(defn container
  [props]
  {:flex               1
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :background-color   (get-background-color props)
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center})

(def container-info
  {:flex-direction :row
   :align-items    :center})

(def user-info
  {:margin-left 8})

(defn style-text-name
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn style-text-value
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/white theme)})

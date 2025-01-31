(ns quo.components.list-items.dapp.style
  (:require
    [quo.foundations.colors :as colors]))

(defn get-background-color
  [{:keys [pressed? state blur? customization-color theme]}]
  (cond
    (and pressed? (= theme :dark) blur?)          colors/white-opa-5

    pressed?                                      (colors/theme-colors
                                                   (colors/override-color customization-color 5 50)
                                                   (colors/override-color customization-color 5 60)
                                                   theme)

    (and (= state :active) (= theme :dark) blur?) colors/white-opa-10

    (= state :active)                             (colors/theme-colors
                                                   (colors/override-color customization-color 10 50)
                                                   (colors/override-color customization-color 10 60)
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
   :align-items    :center
   :flex           1})

(def user-info
  {:margin-left 8})

(defn style-text-name
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn style-text-value
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/white theme)})

(def initials-avatar-container
  {:width  32
   :height 32})

(def image-avatar
  {:width         32
   :height        32
   :border-radius 32})

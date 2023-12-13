(ns quo.components.tags.summary-tag.style
  (:require
    [quo.foundations.colors :as colors]))

(defn main
  [{:keys [type theme customization-color]}]
  {:justify-content  :center
   :align-items      :center
   :height           32
   :padding-left     4
   :padding-right    10
   :flex-direction   :row
   :border-radius    (if (#{:account :collectible} type) 10 16)
   :background-color (colors/resolve-color customization-color theme 10)})

(defn label
  [type theme]
  {:color       (colors/theme-colors colors/neutral-100 colors/white theme)
   :margin-left (if (= type :address) 6 4)})

(def collectible-image
  {:width         24
   :height        24
   :border-radius 10})

(def token-image
  {:border-radius 12})

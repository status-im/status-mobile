(ns quo2.components.group-avatar
  (:require [quo2.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [quo2.components.icon :as icon]
            [quo.react-native :as rn]))

(def sizes
  {:icon {:small 12
          :medium 16
          :large 20}
   :container {:small 20
               :medium 32
               :large 48}})

(defn group-avatar [_]
  (fn [{:keys [color size override-theme]}]
    (let [theme (or override-theme (quo.theme/get-theme))
          container-size (get-in sizes [:container size])
          icon-size (get-in sizes [:icon size])]
      [rn/view {:width container-size
                :height container-size
                :align-items :center
                :justify-content :center
                :border-radius (/ container-size 2)
                :background-color (colors/custom-color color theme)}
       [icon/icon :total-members {:size icon-size
                                  :color colors/white-opa-70}]])))

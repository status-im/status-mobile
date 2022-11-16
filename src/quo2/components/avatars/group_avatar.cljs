(ns quo2.components.avatars.group-avatar
  (:require [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [react-native.core :as rn]))

(def sizes
  {:icon      {:small  12
               :medium 16
               :large  20}
   :container {:small  20
               :medium 32
               :large  48}})

;; TODO: this implementation does not support group display picture (can only display default group icon).
(defn group-avatar [_]
  (fn [{:keys [color size]}]
    (let [container-size (get-in sizes [:container size])
          icon-size      (get-in sizes [:icon size])]
      [rn/view {:width            container-size
                :height           container-size
                :align-items      :center
                :justify-content  :center
                :border-radius    (/ container-size 2)
                :background-color (colors/custom-hex-color color 50 60)}
       [icon/icon :i/group {:size  icon-size ; TODO: group icon sizes 12 and 20 (small and large) are missing
                            :color colors/white-opa-70}]])))

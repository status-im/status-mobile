(ns quo2.components.avatars.group-avatar
  (:require [quo2.components.icon :as icon]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def sizes
  {:icon      {:small  12
               :medium 16
               :large  20}
   :container {:small  20
               :medium 32
               :large  48}})

;; TODO: this implementation does not support group display picture (can only display default group
;; icon).
(defn group-avatar
  [_]
  (fn [{:keys [color size]}]
    (let [container-size (get-in sizes [:container size])
          icon-size      (get-in sizes [:icon size])]
      [rn/view
       {:width            container-size
        :height           container-size
        :align-items      :center
        :justify-content  :center
        :border-radius    (/ container-size 2)
        ;:background-color (colors/custom-color-by-theme color 50 60) ; TODO: this is temporary only.
        ;Issue: https://github.com/status-im/status-mobile/issues/14566
        :background-color color}
       [icon/icon :i/group
        {:size  icon-size
         :color colors/white-opa-70}]])))

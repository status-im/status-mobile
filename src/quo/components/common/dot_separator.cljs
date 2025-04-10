(ns quo.components.common.dot-separator
  (:require [quo.context :as quo.context]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn view
  [{:keys [color size container-style]
    :or   {size 2}}]
  (let [theme         (quo.context/use-theme)
        default-color (colors/theme-colors colors/neutral-80-opa-60 colors/neutral-40 theme)]
    [rn/view
     {:style (merge {:padding-horizontal 4
                     :align-items        :center
                     :justify-content    :center}
                    container-style)}
     [rn/view
      {:style {:width            size
               :height           size
               :border-radius    (/ size 2)
               :background-color (or color default-color)}}]]))

(ns quo2.components.filter-tags
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.filter-tag :as filter-tag]))

(defn tags [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size with-label type] :or {size 32}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [label id emoji icon]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size 32) 12 8)}
            [filter-tag/tag
             (merge {:id             id
                     :size           size
                     :with-label     with-label
                     :active        (= id active-id)
                     :on-press #(do (reset! active-tab-id %)
                                    (when on-change
                                      (on-change %)))}
                    (if (= type :icon)
                      {:icon         icon}
                      {:emoji         emoji}))
             label]])]))))
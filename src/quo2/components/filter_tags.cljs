(ns quo2.components.filter-tags
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.filter-tag :as tag]))

(defn tags [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size type label disabled] :or {size 32}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [tag-label id emoji icon]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size 32) 12 8)}
            [tag/filter-tag
             (merge {:id            id
                     :size          size
                     :label         (if (or (= type :text) label) tag-label nil)
                     :active        (= id active-id)
                     :disabled      disabled
                     :on-press      #(do (reset! active-tab-id %)
                                         (when on-change (on-change %)))}
                    (when-not (and (= type :text) tag-label)
                      (if (= type :icon)
                        {:icon         icon}
                        {:emoji         emoji})))
             tag-label]])]))))
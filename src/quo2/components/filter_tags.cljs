(ns quo2.components.filter-tags
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.filter-tag :as tag]))

(defn tags [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size type labelled disabled blurred] :or {size 32}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [tag-label id resource]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size 32) 12 8)}
            [tag/filter-tag
             (merge {:id            id
                     :size          size
                     :type          type
                     :label         (if labelled tag-label (when (= type :label) tag-label))
                     :active        (= id active-id)
                     :disabled      disabled
                     :blurred       blurred
                     :labelled      (if (= type :label) true labelled)
                     :resource      (if (= type :icon)
                                      :main-icons2/placeholder
                                      resource)
                     :on-press      #(do (reset! active-tab-id %)
                                         (when on-change (on-change %)))})
             tag-label]])]))))
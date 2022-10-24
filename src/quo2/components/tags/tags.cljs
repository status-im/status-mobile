(ns quo2.components.tags.tags
  (:require [quo2.components.tags.tag :as tag]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn tags
  [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size type labelled? disabled? blurred? icon-color] :or {size 32}}]
      (let [active-id @active-tab-id]
        [rn/view {:style {:flex-direction :row}}
         (for [{:keys [label id resource]} data]
           ^{:key id}
           [rn/view {:style {:margin-right 8}}
            [tag/tag
             (merge {:id            id
                     :size          size
                     :type          type
                     :label         (if labelled? label (when (= type :label) label))
                     :active        (= id active-id)
                     :disabled?     disabled?
                     :blurred?      blurred?
                     :icon-color    icon-color
                     :labelled?      (if (= type :label) true labelled?)
                     :resource      (if (= type :icon)
                                      :i/placeholder
                                      resource)
                     :on-press      #(do (reset! active-tab-id %)
                                         (when on-change (on-change %)))})]])]))))

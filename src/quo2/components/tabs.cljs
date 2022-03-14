(ns quo2.components.tabs
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.tab :as tab]))

(defn tabs [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size] :or {size 32}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [label id]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size 32) 12 8)}
            [tab/tab
             {:id id
              :size size
              :active (= id active-id)
              :on-press #(do (reset! active-tab-id %)
                             (when on-change
                               (on-change %)))}
             label]])]))))
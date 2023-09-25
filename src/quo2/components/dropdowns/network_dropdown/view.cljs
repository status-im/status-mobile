(ns quo2.components.dropdowns.network-dropdown.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.dropdowns.network-dropdown.style :as style]
            [react-native.core :as rn]
            [quo2.components.list-items.preview-list.view :as preview-list]
            [reagent.core :as reagent]))

(defn- internal-view
  [_ _]
  (let [pressed? (reagent/atom false)]
    (fn
      [{:keys [on-press state] :as props} networks]
      [rn/pressable
       {:style               (style/dropdown-container (merge props {:pressed? @pressed?}))
        :accessibility-label :network-dropdown
        :disabled            (= state :disabled)
        :on-press            on-press
        :on-press-in         (fn [] (reset! pressed? true))
        :on-press-out        (fn [] (reset! pressed? false))}
       [preview-list/view
        {:type      :network
         :list-size (count networks)
         :size      :size-20}
        networks]])))

(def view (quo.theme/with-theme internal-view))

(ns quo.components.dropdowns.network-dropdown.view
  (:require
    [quo.components.dropdowns.network-dropdown.style :as style]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]
    [reagent.core :as reagent]))

(defn- view-pure
  [{:keys [on-press state] :as props} networks]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn.pure/use-state false)]
    (rn.pure/pressable
     {:style               (style/dropdown-container (merge props {:pressed? pressed?}) theme)
      :accessibility-label :network-dropdown
      :disabled            (= state :disabled)
      :on-press            on-press
      :on-press-in         #(set-pressed true)
      :on-press-out        #(set-pressed false)}
     (reagent/as-element
      [preview-list/view
       {:type      :network
        :list-size (count networks)
        :size      :size-20}
       networks]))))

(defn view [props networks] (rn.pure/func view-pure props networks))

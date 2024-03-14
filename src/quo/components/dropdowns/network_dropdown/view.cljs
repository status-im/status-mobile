(ns quo.components.dropdowns.network-dropdown.view
  (:require
    [quo.components.dropdowns.network-dropdown.style :as style]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [on-press state] :as props} networks]
  (let [theme                  (quo.theme/use-theme-value)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style               (style/dropdown-container (merge props {:pressed? pressed? :theme theme}))
      :accessibility-label :network-dropdown
      :disabled            (= state :disabled)
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
     [preview-list/view
      {:type      :network
       :list-size (count networks)
       :size      :size-20}
      networks]]))

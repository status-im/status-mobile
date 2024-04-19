(ns quo.components.list-items.dapp.view
  (:require
    [quo.components.icon :as icons]
    [quo.components.list-items.dapp.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn view
  [{:keys [dapp action on-press on-press-icon] :as props}]
  (let [theme                  (quo.theme/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style        (style/container (assoc props :pressed? pressed?))
      :on-press     on-press
      :on-press-in  on-press-in
      :on-press-out on-press-out}
     [rn/view {:style style/container-info}
      [fast-image/fast-image
       {:source (:avatar dapp)
        :style  {:width 32 :height 32}}]
      [rn/view {:style style/user-info}
       [text/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  (style/style-text-name theme)}
        (:name dapp)]
       [text/text
        {:weight :regular
         :size   :paragraph-2
         :style  (style/style-text-value theme)}
        (:value dapp)]]]
     (when (= action :icon)
       [rn/pressable
        {:on-press on-press-icon
         :testID   "dapp-component-icon"}
        [icons/icon :i/options
         {:color               (colors/theme-colors
                                colors/neutral-50
                                colors/neutral-40
                                theme)
          :accessibility-label :icon}]])]))

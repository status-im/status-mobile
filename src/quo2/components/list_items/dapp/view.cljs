(ns quo2.components.list-items.dapp.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [quo2.components.list-items.dapp.style :as style]
            [quo2.theme :as quo.theme]
            [reagent.core :as reagent]))

(defn- view-internal
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [dapp action on-press on-press-icon theme] :as props}]
      [rn/pressable
       {:style        (style/container (assoc props :pressed? @pressed?))
        :on-press     on-press
        :on-press-in  #(reset! pressed? true)
        :on-press-out #(reset! pressed? false)}
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
            :accessibility-label :icon}]])])))

(def view
  (quo.theme/with-theme view-internal))

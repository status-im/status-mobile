(ns quo2.components.buttons.wallet-button.view
  (:require
    [quo2.components.buttons.wallet-button.style :as style]
    [quo2.components.icon :as quo2.icons]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- view-internal
  []
  (let [pressed? (reagent/atom false)]
    (fn
      [{:keys [on-press on-long-press disabled? icon accessibility-label container-style theme]}]
      [rn/pressable
       {:accessibility-label (or accessibility-label :wallet-button)
        :on-press            on-press
        :on-press-in         #(reset! pressed? true)
        :on-press-out        #(reset! pressed? nil)
        :on-long-press       on-long-press
        :disabled            disabled?
        :style               (merge (style/main {:pressed?  @pressed?
                                                 :theme     theme
                                                 :disabled? disabled?})
                                    container-style)}
       [quo2.icons/icon icon
        {:color (colors/theme-colors colors/neutral-100 colors/white theme)}]])))

(def view (theme/with-theme view-internal))

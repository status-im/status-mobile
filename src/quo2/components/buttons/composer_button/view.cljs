(ns quo2.components.buttons.composer-button.view
  (:require [quo2.components.icon :as quo2.icons]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.components.buttons.composer-button.style :as style]))

(defn- view-internal
  [_ _]
  (let [pressed? (reagent/atom false)]
    (fn
      [{:keys [on-press on-long-press disabled? theme blur? icon accessibility-label container-style]}]
      [rn/pressable
       {:accessibility-label (or accessibility-label :composer-button)
        :on-press            on-press
        :on-press-in         #(reset! pressed? true)
        :on-press-out        #(reset! pressed? nil)
        :on-long-press       on-long-press
        :disabled            disabled?
        :style               (merge (style/main {:pressed?  @pressed?
                                                 :blur?     blur?
                                                 :theme     theme
                                                 :disabled? disabled?})
                                    container-style)}
       [quo2.icons/icon icon
        {:color (style/get-label-color {:blur? blur?
                                        :theme theme})}]])))

(def view (theme/with-theme view-internal))

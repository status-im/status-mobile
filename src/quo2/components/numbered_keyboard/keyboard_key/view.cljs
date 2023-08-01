(ns quo2.components.numbered-keyboard.keyboard-key.view
  (:require [react-native.core :as rn]
            [quo2.theme :as quo.theme]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icons]
            [quo2.components.numbered-keyboard.keyboard-key.style :as style]
            [reagent.core :as reagent]))

(defn- keyboard-key-internal
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [disabled? theme blur? on-press type]} label]
      (let [label-color      (style/get-label-color disabled? theme blur?)
            background-color (style/toggle-background-color @pressed? blur? theme)]
        [rn/pressable
         {:accessibility-label :keyboard-key
          :disabled            disabled?
          :on-press            on-press
          :on-press-in         #(reset! pressed? true)
          :on-press-out        #(reset! pressed? false)
          :style               (style/container background-color)}
         (condp = type
           :key             [icons/icon
                             label
                             {:color               label-color
                              :accessibility-label :icon-label}]
           :digit           [text/text
                             {:accessibility-label :text-label
                              :weight              :regular
                              :size                :heading-1
                              :style               {:color label-color}}
                             label]
           :derivation-path [icons/icon
                             :i/derivation-path
                             {:color               label-color
                              :size                32
                              :accessibility-label :derivation-path-label}])]))))

(def keyboard-key (quo.theme/with-theme keyboard-key-internal))

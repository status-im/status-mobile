(ns quo.components.numbered-keyboard.keyboard-key.view
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.numbered-keyboard.keyboard-key.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- label->accessibility-label
  [label]
  (let [label-name (if (keyword? label) (name label) label)]
    (keyword (str "keyboard-key-" label-name))))

(defn- view-internal
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [disabled? theme blur? on-press type]} label]
      (let [label-color      (style/get-label-color disabled? theme blur?)
            background-color (style/toggle-background-color @pressed? blur? theme)]
        [rn/pressable
         {:accessibility-label (label->accessibility-label label)
          :disabled            (or disabled? (not label))
          :on-press            (fn []
                                 (when on-press
                                   (on-press label)))
          :on-press-in         #(reset! pressed? true)
          :on-press-out        #(reset! pressed? false)
          :hit-slop            {:top 8 :bottom 8 :left 25 :right 25}
          :style               (style/container background-color)}
         (case type
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
                              :accessibility-label :derivation-path-label}]
           nil)]))))

(def view (quo.theme/with-theme view-internal))

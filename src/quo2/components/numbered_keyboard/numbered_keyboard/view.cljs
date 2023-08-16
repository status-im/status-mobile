(ns quo2.components.numbered-keyboard.numbered-keyboard.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.numbered-keyboard.keyboard-key.view :as keyboard-key]
            [react-native.core :as rn]
            [quo2.components.numbered-keyboard.numbered-keyboard.style :as style]))

(defn keyboard-item
  [{:keys [item type disabled? on-press blur? theme]}]
  [keyboard-key/view
   {:disabled? disabled?
    :on-press  on-press
    :blur?     blur?
    :theme     theme
    :type      type}
   item])

(defn- view-internal
  []
  (fn [{:keys [disabled? theme blur? left-action delete-key? on-press]}]
    [rn/view
     {:style style/container}
     (for [row-index (range 1 4)]
       ^{:key row-index}
       [rn/view {:style style/row-container}
        (for [column-index (range 1 4)]
          [keyboard-item
           {:item      (+ (* (dec row-index) 3) column-index)
            :type      :digit
            :disabled? disabled?
            :on-press  on-press
            :blur?     blur?
            :theme     theme}])])
     ;; bottom row
     [rn/view {:style style/row-container}
      (case left-action
        :dot     [keyboard-item
                  {:item      "."
                   :type      :digit
                   :disabled? disabled?
                   :on-press  on-press
                   :blur?     blur?
                   :theme     theme}]
        :face-id [keyboard-item
                  {:item      :i/faceid-key
                   :type      :key
                   :disabled? disabled?
                   :on-press  on-press
                   :blur?     blur?
                   :theme     theme}]
        :none    [keyboard-item])
      [keyboard-item
       {:item      "0"
        :type      :digit
        :disabled? disabled?
        :on-press  on-press
        :blur?     blur?
        :theme     theme}]
      (if delete-key?
        [keyboard-item
         {:item      :i/delete
          :type      :key
          :disabled? disabled?
          :on-press  on-press
          :blur?     blur?
          :theme     theme}]
        [keyboard-item])]]))

(def view (quo.theme/with-theme view-internal))

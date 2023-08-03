(ns quo2.components.numbered-keyboard.numbered-keyboard.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.numbered-keyboard.keyboard-key.view :as quo]
            [react-native.core :as rn]
            [quo2.components.numbered-keyboard.numbered-keyboard.style :as style]))

(defn keyboard-item
  [{:keys [item type position disabled? on-press blur? theme]
    :or   {position 1
           item     nil}}]
  [rn/view
   {:style (style/keyboard-item position)}
   (when item
     [quo/keyboard-key
      {:disabled? disabled?
       :on-press  on-press
       :blur?     blur?
       :theme     theme
       :type      type} item])])

(defn- numbered-keyboard-internal
  []
  (fn [{:keys [disabled? theme blur? left-action delete-key? on-press]}]
    [rn/view
     {:style style/container}
     (for [item (range 1 10)]
       ^{:key item}
       [keyboard-item
        {:item      item
         :type      :digit
         :position  item
         :disabled? disabled?
         :on-press  on-press
         :blur?     blur?
         :theme     theme}])
     (condp = left-action
       :dot     [keyboard-item
                 {:item      "."
                  :type      :digit
                  :disabled? disabled?
                  :on-press  on-press
                  :blur?     blur?
                  :theme     theme}]
       :face-id [keyboard-item
                 {:item      :i/face-id
                  :type      :key
                  :disabled? disabled?
                  :on-press  on-press
                  :blur?     blur?
                  :theme     theme}]
       :none    [keyboard-item])
     [keyboard-item
      {:item      "0"
       :type      :digit
       :position  2
       :disabled? disabled?
       :on-press  on-press
       :blur?     blur?
       :theme     theme}]
     (if delete-key?
       [keyboard-item
        {:item      :i/delete
         :type      :key
         :position  3
         :disabled? disabled?
         :on-press  on-press
         :blur?     blur?
         :theme     theme}]
       [keyboard-item])]))

(def numbered-keyboard (quo.theme/with-theme numbered-keyboard-internal))

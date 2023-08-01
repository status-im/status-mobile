(ns quo2.components.numbered-keyboard.numbered-keyboard.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.numbered-keyboard.keyboard-key.view :as quo]
            [react-native.core :as rn]
            [quo2.components.numbered-keyboard.numbered-keyboard.style :as style]))

(defn keyboard-item
  [item type disabled? on-press blur? position theme]
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
       [keyboard-item item :digit disabled? on-press blur? item theme])
     (condp = left-action
       :dot     [keyboard-item "." :digit disabled? on-press blur? 1 theme]
       :face-id [keyboard-item :i/face-id :key disabled? on-press blur? 1 theme]
       :none    [keyboard-item nil])
     [keyboard-item "0" :digit disabled? on-press blur? 2 theme]
     (if delete-key?
       [keyboard-item :i/delete :key disabled? on-press blur? 3 theme]
       [keyboard-item nil])]))

(def numbered-keyboard (quo.theme/with-theme numbered-keyboard-internal))

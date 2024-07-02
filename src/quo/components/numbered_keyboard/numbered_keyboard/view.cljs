(ns quo.components.numbered-keyboard.numbered-keyboard.view
  (:require
    [quo.components.numbered-keyboard.keyboard-key.view :as keyboard-key]
    [quo.components.numbered-keyboard.numbered-keyboard.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn keyboard-item
  [{:keys [item type disabled? on-press on-long-press blur? theme]}]
  [keyboard-key/view
   {:disabled?     disabled?
    :on-press      on-press
    :on-long-press on-long-press
    :blur?         blur?
    :theme         theme
    :type          type}
   item])

(defn view
  []
  (fn [{:keys [disabled? blur? left-action delete-key? on-press on-delete on-long-press-delete
               container-style]
        :or   {left-action :none}}]
    (let [theme (quo.theme/use-theme)]
      [rn/view
       {:style (merge style/container
                      container-style)}
       (for [row-index (range 1 4)]
         ^{:key row-index}
         [rn/view {:style style/row-container}
          (for [column-index (range 1 4)]
            ^{:key (str row-index column-index)}
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
           {:item          :i/backspace
            :type          :key
            :disabled?     disabled?
            :on-press      on-delete
            :on-long-press on-long-press-delete
            :blur?         blur?
            :theme         theme}]
          [keyboard-item])]])))

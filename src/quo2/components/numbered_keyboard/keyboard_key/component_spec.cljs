(ns quo2.components.numbered-keyboard.keyboard-key.component-spec
  (:require [quo2.components.numbered-keyboard.keyboard-key.view :as component]
            [test-helpers.component :as h]))

(h/describe "Keyboard Key"
  (h/test "render digit type"
    (h/render [component/view
               {:disabled? false
                :on-press  #(js/alert "pressed")
                :blur?     false
                :type      :digit} 1])
    (h/is-truthy (h/query-by-label-text :text-label)))

  (h/test "render key type"
    (h/render [component/view
               {:disabled? false
                :on-press  #(js/alert "pressed")
                :blur?     false
                :type      :key} :i/delete])
    (h/is-truthy (h/query-by-label-text :icon-label)))

  (h/test "render derivation path type"
    (h/render [component/view
               {:disabled? false
                :on-press  #(js/alert "pressed")
                :blur?     false
                :type      :derivation-path}])
    (h/is-truthy (h/query-by-label-text :derivation-path-label)))

  (h/test "Is pressable when disabled is false"
    (let [on-press (h/mock-fn)]
      (h/render [component/view
                 {:disabled? false
                  :on-press  #(on-press)
                  :blur?     false
                  :type      :digit} 1])
      (h/is-truthy (h/query-by-label-text :text-label))
      (h/fire-event :press (h/query-by-label-text :keyboard-key))
      (h/was-called on-press)))

  (h/test "Is not pressable when disabled is true"
    (let [on-press (h/mock-fn)]
      (h/render [component/view
                 {:disabled? true
                  :on-press  #(on-press)
                  :blur?     false
                  :type      :digit} 1])
      (h/is-truthy (h/query-by-label-text :text-label))
      (h/fire-event :press (h/query-by-label-text :keyboard-key))
      (h/was-not-called on-press))))

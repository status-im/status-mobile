(ns quo.components.buttons.swap-order-button.component-spec
  (:require [quo.components.buttons.swap-order-button.view :as swap-order-button]
            [test-helpers.component :as h]))

(h/describe "Buttons: Swap Order Button"
  (h/test "should render correctly"
    (h/render-with-theme-provider
     [swap-order-button/view {:on-press identity}])
    (h/is-truthy (h/get-by-label-text :swap-order-button)))

  (h/test "should call on-press handler when pressed"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider
       [swap-order-button/view {:on-press on-press}])
      (h/fire-event :press (h/get-by-label-text :swap-order-button))
      (h/was-called on-press)))

  (h/test "should not call on-press handler when button is disabled"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider
       [swap-order-button/view {:on-press on-press :disabled? true}])
      (h/fire-event :press (h/get-by-label-text :swap-order-button))
      (h/was-not-called on-press))))

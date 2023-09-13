(ns quo2.components.buttons.wallet-button.component-spec
  (:require [quo2.components.buttons.wallet-button.view :as wallet-button]
            [test-helpers.component :as h]))

(h/describe "button tests"
  (h/test "default render of wallet button component"
    (h/render [wallet-button/view
               {:icon                :i/placeholder
                :accessibility-label "test-button"}])
    (h/is-truthy (h/get-by-label-text "test-button")))

  (h/test "button on-press works"
    (let [event (h/mock-fn)]
      (h/render [wallet-button/view
                 {:icon                :i/placeholder
                  :on-press            event
                  :accessibility-label "test-button"}])
      (h/fire-event :press (h/get-by-label-text "test-button"))
      (h/was-called event)))

  (h/test "button on-press disabled when disabled"
    (let [event (h/mock-fn)]
      (h/render [wallet-button/view
                 {:icon                :i/placeholder
                  :on-press            event
                  :accessibility-label "test-button"
                  :disabled?           true}])
      (h/fire-event :press (h/get-by-label-text "test-button"))
      (h/was-not-called event)))

  (h/test "button on-long-press works"
    (let [event (h/mock-fn)]
      (h/render [wallet-button/view
                 {:icon                :i/placeholder
                  :on-long-press       event
                  :accessibility-label "test-button"}])
      (h/fire-event :long-press (h/get-by-label-text "test-button"))
      (h/was-called event))))


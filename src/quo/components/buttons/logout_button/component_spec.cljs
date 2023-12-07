(ns quo.components.buttons.logout-button.component-spec
  (:require
    [quo.components.buttons.logout-button.view :as logout-button]
    [test-helpers.component :as h]))

(h/describe "button tests"
  (h/test "default render of logout button component"
    (h/render [logout-button/view])
    (h/is-truthy (h/get-by-label-text :log-out-button)))

  (h/test "button on-press works"
    (let [event (h/mock-fn)]
      (h/render [logout-button/view
                 {:on-press event}])
      (h/fire-event :press (h/get-by-label-text :log-out-button))
      (h/was-called event)))

  (h/test "button on-press disabled when disabled"
    (let [event (h/mock-fn)]
      (h/render [logout-button/view
                 {:on-press  event
                  :disabled? true}])
      (h/fire-event :press (h/get-by-label-text :log-out-button))
      (h/was-not-called event)))

  (h/test "button on-long-press works"
    (let [event (h/mock-fn)]
      (h/render [logout-button/view
                 {:on-long-press event}])
      (h/fire-event :long-press (h/get-by-label-text :log-out-button))
      (h/was-called event))))

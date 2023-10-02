(ns quo2.components.dividers.divider-label.component-spec
  (:require [quo2.components.dividers.divider-label.view :as divider-label]
            [test-helpers.component :as h]))

(h/describe "Divider Label"
  (h/test "default render"
    (h/render [divider-label/view "Welcome"])
    (h/is-truthy (h/query-by-label-text :divider-label)))
  (h/test "with label"
    (h/render [divider-label/view {:tight? true} "Welcome"])
    (h/is-truthy (h/query-by-text "Welcome")))
  (h/test "with label & counter value"
    (h/render [divider-label/view {:tight? true :counter? true :counter-value 5} "Public"])
    (h/is-truthy (h/query-by-text "Public"))
    (h/is-truthy (h/query-by-text "5")))
  (h/test "on-press event"
    (let [on-press (h/mock-fn)]
      (h/render [divider-label/view {:tight? false :on-press on-press} "General"])
      (h/fire-event :press (h/query-by-label-text :divider-label))
      (h/was-called on-press))))

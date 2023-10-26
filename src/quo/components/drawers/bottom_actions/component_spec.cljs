(ns quo.components.drawers.bottom-actions.component-spec
  (:require
    [quo.components.drawers.bottom-actions.view :as bottom-actions]
    [test-helpers.component :as h]))

(h/describe "bottom actions tests"
  (h/test "default render with no description and single action button"
    (h/render [bottom-actions/view {:button-one-label "Request to join"}])
    (h/is-truthy (h/get-by-text "Request to join")))

  (h/test "render with description"
    (h/render [bottom-actions/view {:description "Sample description"}])
    (h/is-truthy (h/get-by-text "Sample description")))

  (h/test "render with 2 actions"
    (let [button-one "Button One"
          button-two "Button Two"]
      (h/render [bottom-actions/view
                 {:actions          :2-actions
                  :button-one-label button-one
                  :button-two-label button-two}])
      (h/is-truthy (h/get-by-text button-one))
      (h/is-truthy (h/get-by-text button-two))))

  (h/test "render disabled button"
    (h/render [bottom-actions/view
               {:description      "Sample description"
                :disabled?        true
                :button-one-label "button"}])
    (h/is-disabled (h/get-by-label-text :button-one))))

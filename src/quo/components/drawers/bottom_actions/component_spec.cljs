(ns quo.components.drawers.bottom-actions.component-spec
  (:require
    [quo.components.drawers.bottom-actions.view :as bottom-actions]
    [test-helpers.component :as h]))

(defn- render
  [component]
  (h/render-with-theme-provider component :light))

(h/describe "bottom actions tests"
  (h/test "default render with no description and single action button"
    (render [bottom-actions/view {:actions :one-action :button-one-label "Request to join"}])
    (h/is-truthy (h/get-by-text "Request to join")))

  (h/test "render with description & 1 action"
    (render [bottom-actions/view
             {:description      :bottom
              :description-text "Sample description"
              :actions          :one-action
              :button-one-label "Request to join"}])
    (h/is-truthy (h/get-by-text "Sample description"))
    (h/is-truthy (h/get-by-text "Request to join")))

  (h/test "render with 2 actions"
    (let [button-one "Button One"
          button-two "Button Two"]
      (render [bottom-actions/view
               {:actions          :two-actions
                :button-one-label button-one
                :button-one-props {:icon-left :i/arrow-left}
                :button-two-label button-two}])
      (h/is-truthy (h/get-by-text button-one))
      (h/is-truthy (h/get-by-label-text :icon))
      (h/is-truthy (h/get-by-text button-two))))

  (h/test "render disabled button"
    (render [bottom-actions/view
             {:description      :bottom
              :actions          :one-action
              :description-text "Sample description"
              :button-one-props {:disabled? true}
              :button-one-label "button"}])
    (h/is-disabled (h/get-by-label-text :button-one)))

  (h/test "render with error & 1 action"
    (render [bottom-actions/view
             {:description      :top-error
              :error-message    "Sample error"
              :actions          :one-action
              :button-one-label "Request to join"}])
    (h/is-truthy (h/get-by-text "Sample error"))
    (h/is-truthy (h/get-by-text "Request to join")))

  (h/test "render with top description & 1 action"
    (render [bottom-actions/view
             {:description      :top
              :role             :admin
              :actions          :one-action
              :button-one-label "Request to join"}])
    (h/is-truthy (h/get-by-text "Eligible to join as"))
    (h/is-truthy (h/get-by-text "Admin"))
    (h/is-truthy (h/get-by-text "Request to join")))

  (h/test "render with top description with custom text & 1 action"
    (render [bottom-actions/view
             {:description          :top
              :role                 :admin
              :actions              :one-action
              :description-top-text "You'll be an"
              :button-one-label     "Request to join"}])
    (h/is-truthy (h/get-by-text "You'll be an"))
    (h/is-truthy (h/get-by-text "Admin"))
    (h/is-truthy (h/get-by-text "Request to join"))))

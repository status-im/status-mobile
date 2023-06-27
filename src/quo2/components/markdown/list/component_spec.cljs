(ns quo2.components.markdown.list.component-spec
  (:require [quo2.components.markdown.list.view :as list]
            [test-helpers.component :as h]))

(h/describe "tests for markdown/list component"
  (h/test "renders component with title"
    (h/render [list/view {:title "test title"}])
    (h/is-truthy (h/get-by-text "test title")))

  (h/test "renders component with description"
    (h/render [list/view
               {:title       "test title"
                :description "test description"}])
    (h/is-truthy (h/get-by-text "test description")))

  (h/test "renders component with title and description"
    (h/render [list/view
               {:title       "test title"
                :description "test description"}])
    (h/is-truthy (h/get-by-text "test title"))
    (h/is-truthy (h/get-by-text "test description")))

  (h/test "renders step component when index is valid"
    (h/render [list/view {:index 1}])
    (h/is-truthy (h/get-by-label-text :step-counter))))

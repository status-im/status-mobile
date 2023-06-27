(ns quo2.components.markdown.list.component-spec
  (:require [quo2.components.markdown.list.view :as list]
            [test-helpers.component :as h]
            [react-native.core :as rn]
            [quo2.core :as quo]))

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
    (h/is-truthy (h/get-by-label-text :step-counter)))

  (h/test "renders decription with a context tag component"
    (h/render [list/view
               {:index       1
                :description [rn/view {:style {:flex-direction :row :align-items :center}}
                              [quo/text {} "Lorem ipsum "]
                              [quo/context-tag {:size :small} "some-path" "dolor"]
                              [quo/text {} " sit amet."]]}])
    (h/is-truthy (h/get-by-text "Lorem ipsum"))
    (h/is-truthy (h/get-by-label-text :user-avatar))))

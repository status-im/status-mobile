(ns quo.components.markdown.list.component-spec
  (:require
    [quo.components.markdown.list.view :as list]
    [test-helpers.component :as h]))

(h/describe "tests for markdown/list component"
  (h/test "renders component with title"
    (h/render-with-theme-provider [list/view {:title "test title"}])
    (h/is-truthy (h/get-by-text "test title")))

  (h/test "renders component with description"
    (h/render-with-theme-provider [list/view
                                   {:title       "test title"
                                    :description "test description"}])
    (h/is-truthy (h/get-by-text "test description")))

  (h/test "renders component with title and description"
    (h/render-with-theme-provider [list/view
                                   {:title       "test title"
                                    :description "test description"}])
    (h/is-truthy (h/get-by-text "test title"))
    (h/is-truthy (h/get-by-text "test description")))

  (h/test "renders step component when step-number is valid and type is step"
    (h/render-with-theme-provider [list/view
                                   {:type        :step
                                    :step-number 1}]
                                  :dark)
    (h/is-truthy (h/get-by-label-text :step-counter)))

  (h/test "renders decription with a context tag component and description after the tag"
    (h/render-with-theme-provider [list/view
                                   {:step-number           1
                                    :description           "Lorem ipsum "
                                    :tag-name              "dolor"
                                    :description-after-tag "text after tag"}])
    (h/is-truthy (h/get-by-text "Lorem ipsum"))
    (h/is-truthy (h/get-by-label-text :user-avatar))
    (h/is-truthy (h/get-by-text "dolor"))
    (h/is-truthy (h/get-by-text "text after tag"))))

(ns quo.components.list-items.status-list-item.component-spec
  (:require
    [quo.components.list-items.status-list-item.view :as status-list-item]
    [test-helpers.component :as h]))

(h/describe "Status List Item"
  (h/test "With title and description"
    (h/render [status-list-item/view
               {:status      :positive
                :title       "Account"
                :description "This is a description"}])
    (h/is-truthy (h/get-by-text "Account"))
    (h/is-truthy (h/get-by-text "This is a description"))))

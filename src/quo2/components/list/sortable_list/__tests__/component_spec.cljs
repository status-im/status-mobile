(ns quo2.components.list.sortable-list.--tests--.component-spec
  (:require [quo2.components.list.sortable-list.view :as sortable-list]
            [test-helpers.component :as h]))

(def data [{:id 1 :type "item" :label "Item 1"}
            {:id 2 :type "item" :label "Item 2"}
            {:id 3 :type "item" :label "Item 3"}])

(h/describe "sortable list"
  (h/test "render component"
    (h/render [sortable-list/reorder-list data])
    (-> (h/expect (h/get-by-label-text "Item 1"))
        (.toBeTruthy))))

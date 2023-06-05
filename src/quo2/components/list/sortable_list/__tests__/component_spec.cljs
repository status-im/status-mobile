(ns quo2.components.list.sortable-list.--tests--.component-spec
  (:require [quo2.components.list.sortable-list.view :as sortable-list]
            [reagent.core :as reagent]
            [test-helpers.component :as h]))

(h/describe "Sortable list"
  (h/test "Default render"
    (h/render [sortable-list/reorder-list])
            (h/is-falsy (h/get-by-test-id "Pinterest"))))
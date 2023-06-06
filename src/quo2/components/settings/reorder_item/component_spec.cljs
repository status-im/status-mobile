(ns quo2.components.settings.reorder-item.component-spec
  (:require [quo2.components.settings.reorder-item.view :as sortable-list]
            [quo2.components.settings.reorder-item.items.item :as item]
            [test-helpers.component :as h]))

(def data [{:id 1 :type "item" :label "Item 1"}
           {:id 2 :type "item" :label "Item 2"}
           {:id 3 :type "item" :label "Item 3"}])

(h/describe "sortable list items tests"
            (h/test "renders item"
                    (h/debug (h/render [item/view {:id 1 :type "item" :label "Item 1"}]))
                    (h/is-truthy (h/get-by-text "Item 1"))))


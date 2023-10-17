(ns quo.components.settings.reorder-item.component-spec
  (:require
    [quo.components.settings.reorder-item.types :as types]
    [quo.core :as quo]
    [test-helpers.component :as h]))

(h/describe
  "sortable list items tests"
  (h/test "renders item"
    (h/render [quo/reorder-item
               {:id         1
                :type       "item"
                :image-size 24
                :title      "Item 1"} types/item])
    (h/is-truthy (h/get-by-text "Item 1")))

  (h/test "renders item placeholder"
    (h/render [quo/reorder-item {:label "Item 1"} types/placeholder])
    (h/is-truthy (h/get-by-text "Item 1")))

  (h/test "renders item skeleton"
    (let [component (h/render [quo/reorder-item nil types/skeleton])]
      (h/is-truthy component)))

  (h/test "renders item tab"
    (h/render [quo/reorder-item
               {:data [{:id    1
                        :label "Item 1"}
                      ]} types/tab])
    (h/is-truthy (h/get-by-text "Item 1"))))

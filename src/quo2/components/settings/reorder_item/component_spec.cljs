(ns quo2.components.settings.reorder-item.component-spec
  (:require [quo2.components.settings.reorder-item.items.item :as item]
            [quo2.components.settings.reorder-item.items.item-placeholder :as placeholder]
            [quo2.components.settings.reorder-item.items.item-skeleton :as skeleton]
            [quo2.components.settings.reorder-item.items.item-tabs :as tab]
            [test-helpers.component :as h]))

(h/describe
  "sortable list items tests"
  (h/test "renders item"
    (h/render [item/view
               {:id         1
                :type       "item"
                :image-size 24
                :title      "Item 1"}])
    (h/is-truthy (h/get-by-text "Item 1")))

  (h/test "renders item placeholder"
    (h/render [placeholder/view "Item 1" false])
    (h/is-truthy (h/get-by-text "Item 1")))

  (h/test "renders item skeleton"
    (let [component (h/render [skeleton/view])]
      (h/is-truthy component)))

  (h/test "renders item tab"
    (h/render [tab/view
               [{:id    1
                 :label "Item 1"}] 1])
    (h/is-truthy (h/get-by-text "Item 1"))))
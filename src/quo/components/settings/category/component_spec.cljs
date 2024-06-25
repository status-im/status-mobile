(ns quo.components.settings.category.component-spec
  (:require
    [quo.components.settings.category.view :as category]
    [test-helpers.component :as h]))

(h/describe "Settings Category tests"
  (h/test "category label renders"
    (h/render [category/category
               {:list-type :settings
                :label     "Label"
                :data      [{:title     "Item 1"
                             :left-icon :i/browser
                             :chevron?  true}]}])
    (h/is-truthy (h/get-by-text "Label")))

  (h/test "category item renders"
    (h/render [category/category
               {:list-type :settings
                :label     "Label"
                :data      [{:title     "Item 1"
                             :left-icon :i/browser
                             :chevron?  true}]}])
    (h/is-truthy (h/get-by-text "Item 1"))))


(h/describe "Reorder Category tests"
  (h/test "category label renders"
    (h/render [category/category
               {:list-type :reorder
                :label     "Label"
                :data      [{:title      "Item 1"
                             :right-icon :i/globe
                             :chevron?   true}]}])
    (h/is-truthy (h/get-by-text "Label")))

  (h/test "category item renders"
    (h/render [category/category
               {:list-type :reorder
                :label     "Label"
                :data      [{:title      "Item 1"
                             :right-icon :i/globe
                             :chevron?   true}]}])
    (h/is-truthy (h/get-by-text "Item 1")))

  (h/test "category item subtitle renders"
    (h/render [category/category
               {:list-type :reorder
                :label     "Label"
                :data      [{:title      "Item 1"
                             :subtitle   "subtitle"
                             :right-icon :i/globe
                             :chevron?   true}]}])
    (h/is-truthy (h/get-by-text "subtitle"))))

(h/describe "Data Item Category tests"
  (h/test "category renders"
    (h/render [category/category
               {:list-type :data-item
                :label     "Label"
                :data      [{:title      "Item 1"
                             :subtitle   "subtitle"
                             :right-icon :i/globe}]}])
    (h/is-truthy (h/get-by-text "Label"))
    (h/is-truthy (h/get-by-text "Item 1"))
    (h/is-truthy (h/get-by-text "subtitle")))

  (h/test "category renders without label"
    (h/render [category/category
               {:list-type :data-item
                :data      [{:title      "Item 1"
                             :subtitle   "subtitle"
                             :right-icon :i/globe}]}])
    (h/is-falsy (h/query-by-label-text "Label"))
    (h/is-truthy (h/get-by-text "Item 1"))))

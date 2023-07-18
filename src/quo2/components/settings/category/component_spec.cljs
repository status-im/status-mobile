(ns quo2.components.settings.category.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.settings.category.view :as category]))

(h/describe "button tests"
  (h/test "category label renders"
    (h/render [category/category
               {:label "label"
                :data  [{:title     "Item 1"
                         :left-icon :i/browser
                         :chevron?  true}]}])
    (h/is-truthy (h/get-by-text "label")))

  (h/test "category item renders"
    (h/render [category/category
               {:label "label"
                :data  [{:title     "Item 1"
                         :left-icon :i/browser
                         :chevron?  true}]}])
    (h/is-truthy (h/get-by-text "Item 1"))))

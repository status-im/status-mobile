(ns quo2.components.settings.category.component-spec
  (:require [quo2.components.settings.category.view :as category]
            [test-helpers.component :as h]))

(h/describe "Category list tests"
  (h/test "Default render a Label on list component"
    (h/render [category/category {:label "Gender"}])
    (h/is-truthy (h/get-by-label-text :Gender)))

  (h/test "It renders a list with items"
    (h/render [category/category
               {:settings-list-data [{:title               "Male"
                                      :accessibility-label :settings-list-item
                                      :left-icon           :browser-context
                                      :chevron?            true
                                      :border              true
                                      :on-press            (fn [] (js/alert "Male pressed"))}
                                     {:title               "Female"
                                      :accessibility-label :settings-list-item
                                      :left-icon           :browser-context
                                      :chevron?            false
                                      :border              true
                                      :on-press            (fn [] (js/alert "Female pressed"))}]}])
    (h/is-truthy (h/get-by-text :title))))
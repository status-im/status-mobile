(ns quo2.components.switchers.base-card.component-spec
  (:require
    [quo2.components.switchers.base-card.view :as base-card]
    [quo2.foundations.colors :as colors]
    [test-helpers.component :as h]))

(h/describe "Switcher: Base card"
  (h/test "Default render"
    (h/render [base-card/base-card {}])
    (h/is-truthy (h/query-by-label-text :base-card)))
  (h/test "Banner render"
    (h/render [base-card/base-card {:banner {:source "banner"}}])
    (h/is-truthy (h/query-by-label-text :base-card))
    (h/is-truthy (h/query-by-label-text :banner)))
  (h/test "Customization color"
    (h/render [base-card/base-card {:customization-color :blue}])
    (h/has-style
     (h/query-by-label-text :base-card)
     {:backgroundColor (colors/custom-color :blue 50 40)})))

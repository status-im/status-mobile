(ns quo2.components.gradient.gradient-cover.component-spec
  (:require [quo2.components.gradient.gradient-cover.view :as gradient-cover]
            [test-helpers.component :as h]))

(h/describe "gradient cover"
  (h/test "default render"
    (h/render [gradient-cover/view])
    (h/is-truthy (h/get-by-label-text :gradient-cover))))

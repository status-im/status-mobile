(ns quo2.components.buttons.slide-button.component-spec
  (:require [quo2.components.buttons.slide-button.view :as slide-button]
            [test-helpers.component :as h]))

(h/describe "slide-button"
  (h/test "basic render"
    (h/render [slide-button/view {:size :large :disabled false :label "slide it..."}])
    (h/is-truthy (h/get-by-label-text :slider-button-label)))
  (h/test "rendered with correct text"
    (h/render [slide-button/view {:size :large :disabled false :label "slide it..."}])
    (h/is-truthy (h/get-by-text "slide it...")))
)

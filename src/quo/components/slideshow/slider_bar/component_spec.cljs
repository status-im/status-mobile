(ns quo.components.slideshow.slider-bar.component-spec
  (:require
    [quo.components.slideshow.slider-bar.view :as slider-bar]
    [test-helpers.component :as h]))

(h/describe "Slideshow: slider-bar"
  (h/test "default render"
    (h/render-with-theme-provider [slider-bar/view {:accessibility-label :slider-bar}])
    (h/is-truthy (h/query-by-label-text :slider-bar)))

  (h/test "render with total-amount and active-index"
    (h/render-with-theme-provider [slider-bar/view
                                   {:total-amount        4
                                    :active-index        1
                                    :accessibility-label :slider-bar}])
    (h/is-truthy (h/query-by-label-text :slider-bar))
    (h/is-equal 4 (count (h/query-all-by-label-text :slide-bar-item))))

  (h/test "render with total-amount active-index and possible scroll"
    (h/render-with-theme-provider [slider-bar/view
                                   {:total-amount        10
                                    :active-index        7
                                    :accessibility-label :slider-bar}])
    (h/is-truthy (h/query-by-label-text :slider-bar))
    (h/is-equal 10 (count (h/query-all-by-label-text :slide-bar-item)))))

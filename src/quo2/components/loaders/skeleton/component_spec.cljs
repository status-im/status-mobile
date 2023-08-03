(ns quo2.components.loaders.skeleton.component-spec
  (:require [quo2.components.loaders.skeleton.view :as skeleton]
            [quo2.foundations.colors :as colors]
            [test-helpers.component :as h]))

(h/describe "Skeleton tests"
  (h/test "Skeleton component is animated when animated? is true"
    (h/render [skeleton/skeleton-item 0 :messages colors/neutral-10 true])
    (h/is-truthy (h/get-by-label-text :skeleton-animated)))

  (h/test "Skeleton component is static when animated? is false"
    (h/render [skeleton/skeleton-item 0 :messages colors/neutral-10 false])
    (h/is-truthy (h/get-by-label-text :skeleton-static))))

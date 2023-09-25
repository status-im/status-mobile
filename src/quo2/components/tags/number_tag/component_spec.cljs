(ns quo2.components.tags.number-tag.component-spec
  (:require [quo2.components.tags.number-tag.view :as number-tag]
            [test-helpers.component :as h]))

(h/describe
  "number tag component test"
  (h/test "+3 render"
    (h/render [number-tag/view
               {:type   :rounded
                :number "3"
                :size   :size-32
                :blur?  false}])
    (h/is-truthy (h/get-by-text "+3")))
  (h/test "+48 render"
    (h/render [number-tag/view
               {:type   :squared
                :number "48"
                :size   :size-24
                :blur?  true}])
    (h/is-truthy (h/get-by-text "+48"))))

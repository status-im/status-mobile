(ns quo2.components.tags.tiny-tag.component-spec
  (:require [quo2.components.tags.tiny-tag.view :as tiny-tag]
            [test-helpers.component :as h]))

(h/describe "Tiny tag component test"
  (h/test "1,000 SNT render"
    (h/render [tiny-tag/view
               {:label "1,000 SNT"
                :blur? false}])
    (h/is-truthy (h/get-by-text "1,000 SNT")))
  (h/test "2,000 SNT render with blur"
    (h/render [tiny-tag/view
               {:label "2,000 SNT"
                :blur? true}])
    (h/is-truthy (h/get-by-text "2,000 SNT"))))

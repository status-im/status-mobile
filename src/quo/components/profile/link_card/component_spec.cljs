(ns quo.components.profile.link-card.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))

(h/describe "Link Card Component"
            (h/test "component renders without any props"
                    (h/render [quo/link-card])
                    (h/is-truthy (h/query-by-label-text :link-card))))

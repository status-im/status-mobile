(ns quo.components.profile.link-card.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))

(h/describe "Link Card Component"
  (h/test "component renders without any props"
    (h/render [quo/link-card])
    (h/is-truthy (h/query-by-label-text :link-card)))

  (h/test "component renders with address"
    (h/render [quo/link-card {:address "some address"}])
    (h/is-truthy (h/query-by-label-text :address)))

  (h/test "component renders with link and address"
    (h/render [quo/link-card
               {:link    :link
                :address "some address"}])
    (h/is-truthy (h/query-by-label-text :address))
    (h/is-truthy (h/query-by-label-text :title))
    (h/is-truthy (h/query-by-label-text :website-icon)))

  (h/test "component renders with link (:link) and address"
    (h/render [quo/link-card
               {:link    :link
                :address "some address"}])
    (h/is-truthy (h/query-by-label-text :address))
    (h/is-truthy (h/query-by-label-text :title))
    (h/is-truthy (h/query-by-label-text :website-icon)))

  (h/test "component renders with link (:facebook) and address"
    (h/render [quo/link-card
               {:link    :facebook
                :address "some address"}])
    (h/is-truthy (h/query-by-label-text :address))
    (h/is-truthy (h/query-by-label-text :title))
    (h/is-truthy (h/query-by-label-text :social-icon))))

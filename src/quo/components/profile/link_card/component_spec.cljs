(ns quo.components.profile.link-card.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))

(h/describe "Link Card Component"
  (h/test "component renders with address"
    (h/render [quo/link-card
               {:address "some address"
                :icon    :social/link}])
    (h/is-truthy (h/query-by-label-text :address))
    (h/is-truthy (h/query-by-label-text :social-icon)))

  (h/test "component renders with title and address"
    (h/render [quo/link-card
               {:title   "Website"
                :icon    :social/link
                :address "bento.me/fracesca"}])
    (h/is-truthy (h/query-by-label-text :address))
    (h/is-truthy (h/query-by-label-text :title))
    (h/is-truthy (h/query-by-label-text :social-icon))))

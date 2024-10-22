(ns quo.components.tags.network-status-tag.component-spec
  (:require
    [quo.components.tags.network-status-tag.view :as network-status-tag]
    [test-helpers.component :as h]))

(h/describe "Network status component test"
  (h/test "5 min ago render"
    (h/render [network-status-tag/view
               {:label "Updated 5 min ago"}])
    (h/is-truthy (h/get-by-text "Updated 5 min ago")))
  (h/test "15 min ago render"
    (h/render [network-status-tag/view
               {:label "Updated 15 min ago"}])
    (h/is-truthy (h/get-by-text "Updated 15 min ago"))))

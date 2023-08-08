(ns quo2.components.tags.network-tags.component-spec
  (:require [quo2.components.tags.network-tags.view :as network-tags]
            [test-helpers.component :as h]))

(h/describe "network-tags component"
  (h/test "renders network tags with single network"
    (h/render [network-tags/view
               {:title    "Network Tags"
                :networks [{:source "network-icon.png"}]}])
    (h/is-truthy (h/get-by-text "Network Tags")))

  (h/test "renders network tags with multiple networks"
    (h/render [network-tags/view
               {:title    "Multiple Networks"
                :networks [{:source "network-icon1.png"}
                           {:source "network-icon2.png"}
                           {:source "network-icon3.png"}]
                :size     :size/s-32}])
    (h/is-truthy (h/get-by-text "Multiple Networks"))))

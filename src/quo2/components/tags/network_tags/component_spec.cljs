(ns quo2.components.tags.network-tags.component-spec
  (:require [quo2.components.tags.network-tags.view :as quo2]
            [test-helpers.component :as h]))

(defn render-network-tags
  [opts]
  (h/render [quo2/network-tags opts]))

(h/describe "network-tags component"
  (h/test "renders network tags with single network"
    (render-network-tags {:title    "Network Tags"
                          :networks [{:source "network-icon.png"}]})
    (-> (h/expect (h/get-by-text "Network Tags"))
        (.toBeTruthy)))

  (h/test "renders network tags with multiple networks"
    (render-network-tags {:title    "Multiple Networks"
                          :networks [{:source "network-icon1.png"}
                                     {:source "network-icon2.png"}
                                     {:source "network-icon3.png"}]
                          :size     32})
    (-> (h/expect (h/get-by-text "Multiple Networks"))
        (.toBeTruthy))))

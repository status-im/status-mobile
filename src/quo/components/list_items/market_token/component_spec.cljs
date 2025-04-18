(ns quo.components.list-items.market-token.component-spec
  (:require
    [quo.components.list-items.market-token.view :as market-token]
    [test-helpers.component :as h]))

(h/describe "List Items: Market token"
  (h/test "Render market token"
    (h/render-with-theme-provider [market-token/view
                                   {:token               :snt
                                    :token-rank          10
                                    :token-name          "Status"
                                    :market-cap          "$332.1B"
                                    :price               "$0.8"
                                    :percentage-change   5.2
                                    :customization-color :blue}])
    (h/is-truthy (h/get-by-text "Status")))

  (h/test "Render without props"
    (h/render-with-theme-provider [market-token/view])
    (h/is-truthy (h/get-by-label-text :market-token-container))))

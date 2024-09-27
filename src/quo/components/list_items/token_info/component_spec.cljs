(ns quo.components.list-items.token-info.component-spec
  (:require
    [quo.components.list-items.token-info.view :as token-info]
    [test-helpers.component :as h]))

(h/describe "List Items: Token Info"
  (h/test "Token label renders"
    (h/render-with-theme-provider [token-info/view
                                   {:token               :snt
                                    :label               "Status"
                                    :state               :default
                                    :customization-color :blue}])
    (h/is-truthy (h/get-by-text "Status"))))

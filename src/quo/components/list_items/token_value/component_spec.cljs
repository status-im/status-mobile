(ns quo.components.list-items.token-value.component-spec
  (:require
    [quo.components.list-items.token-value.view :as token-value]
    [test-helpers.component :as h]))

(h/describe "List Items: Token Value"
  (h/test "Token label renders"
    (h/render-with-theme-provider [token-value/view
                                   {:token               :snt
                                    :token-name          "Status"
                                    :state               :default
                                    :status              :empty
                                    :customization-color :blue
                                    :metrics?            true
                                    :values              {:crypto-value      "0.00"
                                                          :fiat-value        "€0.00"
                                                          :percentage-change "0.00"
                                                          :fiat-change       "€0.00"}}])
    (h/is-truthy (h/get-by-text "Status")))

  (h/test "Status change"
    (h/render-with-theme-provider [token-value/view
                                   {:token               :snt
                                    :token-name          "Status"
                                    :state               :default
                                    :status              :positive
                                    :customization-color :blue
                                    :metrics?            true
                                    :values              {:crypto-value      "0.00"
                                                          :fiat-value        "€0.00"
                                                          :percentage-change "0.00"
                                                          :fiat-change       "€0.00"}}])
    (h/is-truthy (h/get-by-label-text :arrow-icon))))

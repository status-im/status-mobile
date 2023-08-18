(ns quo2.components.list-items.token-value.component-spec
  (:require
    [test-helpers.component :as h]
    [quo2.components.list-items.token-value.view :as token-value]))

(h/describe "List Items: Token Value"
  (h/test "Token label renders"
    (h/render [token-value/view
               {:token               :snt
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
    (h/render [token-value/view
               {:token               :snt
                :state               :default
                :status              :positive
                :customization-color :blue
                :metrics?            true
                :values              {:crypto-value      "0.00"
                                      :fiat-value        "€0.00"
                                      :percentage-change "0.00"
                                      :fiat-change       "€0.00"}}])
    (h/is-truthy (h/get-by-label-text :arrow-icon))))

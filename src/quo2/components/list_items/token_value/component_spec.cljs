(ns quo2.components.list-items.token-value.component-spec
  (:require
    [quo2.foundations.colors :as colors]
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

  (h/test "Pressed state"
    (h/render [token-value/view
               {:token               :snt
                :state               :pressed
                :status              :empty
                :customization-color :blue
                :metrics?            true
                :values              {:crypto-value      "0.00"
                                      :fiat-value        "€0.00"
                                      :percentage-change "0.00"
                                      :fiat-change       "€0.00"}}])
    (h/has-style (h/get-by-label-text :container)
                 {:backgroundColor (colors/custom-color-by-theme :blue 50 50 5 5)}))

  (h/test "Active state"
    (h/render [token-value/view
               {:token               :snt
                :state               :active
                :status              :empty
                :customization-color :blue
                :metrics?            true
                :values              {:crypto-value      "0.00"
                                      :fiat-value        "€0.00"
                                      :percentage-change "0.00"
                                      :fiat-change       "€0.00"}}])
    (h/has-style (h/get-by-label-text :container)
                 {:backgroundColor (colors/custom-color-by-theme :blue 50 50 10 10)}))

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

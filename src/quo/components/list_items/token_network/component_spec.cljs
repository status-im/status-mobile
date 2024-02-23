(ns quo.components.list-items.token-network.component-spec
  (:require [quo.components.list-items.token-network.view :as token-network]
            [quo.foundations.colors :as colors]
            [quo.foundations.resources :as resources]
            [test-helpers.component :as h]))

(def props
  {:token       :snt
   :state       :default
   :label       "Status"
   :networks    [{:source (resources/get-network :ethereum)}]
   :token-value "100.00 SNT"
   :fiat-value  "€0.00"})

(h/describe "List items: Token network"
  (h/test "default state"
    (h/render-with-theme-provider [token-network/view (dissoc props :state)])
    (h/is-truthy (h/get-by-text "Status")))

  (h/test "default state explicit"
    (h/render-with-theme-provider [token-network/view props])
    (h/is-truthy (h/get-by-text "Status")))

  (h/test "Pressed state"
    (h/render-with-theme-provider [token-network/view props])
    (h/fire-event :on-press-in (h/get-by-label-text :token-network))
    (h/wait-for #(h/has-style (h/query-by-label-text :token-network)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "Active state"
    (h/render-with-theme-provider [token-network/view (assoc props :state :active)])
    (h/has-style (h/query-by-label-text :token-network)
                 {:backgroundColor (colors/resolve-color :blue :light 10)}))

  (h/test "Selected state"
    (h/render-with-theme-provider [token-network/view (assoc props :state :selected)])
    (h/is-truthy (h/query-by-label-text :check-icon)))

  (h/test "Call on-press"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider [token-network/view (assoc props :on-press on-press)])
      (h/fire-event :on-press (h/get-by-label-text :token-network))
      (h/was-called on-press))))

(ns quo.components.list-items.network-list-item.component-spec
  (:require [quo.components.list-items.network-list-item.view :as network-list-item]
            [quo.foundations.colors :as colors]
            [quo.foundations.resources :as resources]
            [test-helpers.component :as h]))

(def props
  {:token       :ethereum
   :state       :default
   :label       "Mainnet"
   :networks    [(resources/get-network :ethereum)]
   :token-value "100.00 ETH"
   :fiat-value  "€100.00"})

(h/describe "List items: Network List Item"
  (h/test "default state"
    (h/render [network-list-item/view (dissoc props :state)])
    (h/is-truthy (h/get-by-text "Mainnet")))

  (h/test "default state explicit"
    (h/render [network-list-item/view props])
    (h/is-truthy (h/get-by-text "Mainnet")))

  (h/test "Pressed state"
    (h/render [network-list-item/view props])
    (h/fire-event :on-press-in (h/get-by-label-text ::network-list-item))
    (h/wait-for #(h/has-style (h/query-by-label-text ::network-list-item)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "Active state"
    (h/render [network-list-item/view (assoc props :state :active)])
    (h/has-style (h/query-by-label-text ::network-list-item)
                 {:backgroundColor (colors/resolve-color :blue :light 10)}))

  (h/test "Call on-press"
    (let [on-press (h/mock-fn)]
      (h/render [network-list-item/view (assoc props :on-press on-press)])
      (h/fire-event :on-press (h/get-by-label-text ::network-list-item))
      (h/was-called on-press)))

  (h/test "Empty props"
    (h/render [network-list-item/view {}])
    (h/is-truthy (h/get-by-label-text ::network-list-item))))

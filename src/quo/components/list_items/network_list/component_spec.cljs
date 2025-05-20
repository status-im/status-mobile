(ns quo.components.list-items.network-list.component-spec
  (:require [quo.components.list-items.network-list.view :as network-list]
            [quo.foundations.colors :as colors]
            [test-helpers.component :as h]))

(defn- render
  [component]
  (h/render-with-theme-provider component :light))

(def props
  {:state               :default
   :label               "Ethereum"
   :network-image       873
   :customization-color :blue
   :token-value         "100.00 ETH"
   :fiat-value          "€100.00"})


(h/describe "List items: Network List"
  (h/test "default state explicit"
    (render [network-list/view props])
    (h/is-truthy (h/get-by-text "Ethereum")))

  (h/test "Pressed state"
    (render [network-list/view props])
    (h/fire-event :on-press-in (h/get-by-label-text ::network-list))
    (h/wait-for #(h/has-style (h/query-by-label-text ::network-list)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "Active state"
    (render [network-list/view (assoc props :state :active)])
    (h/has-style (h/query-by-label-text ::network-list)
                 {:backgroundColor (colors/resolve-color :blue :light 10)}))

  (h/test "Call on-press"
    (let [on-press (h/mock-fn)]
      (render [network-list/view (assoc props :on-press on-press)])
      (h/fire-event :on-press (h/get-by-label-text ::network-list))
      (h/was-called on-press))))

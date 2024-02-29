(ns quo.components.list-items.address.component-spec
  (:require
    [quo.components.list-items.address.view :as address]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(h/describe "List items: address"
  (h/test "default render"
    (h/render [address/view])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "on-press-in changes state to :pressed"
    (h/render [address/view])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/custom-color :blue 50 5)})))

  (h/test "on-press-in changes state to :pressed with blur? enabled"
    (h/render [address/view {:blur? true}])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor colors/white-opa-5})))

  (h/test "on-press-out changes state to :active"
    (h/render [address/view {:active-state? true}])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/custom-color :blue 50 10)})))

  (h/test "on-press-out changes state to :active with blur? enabled"
    (h/render [address/view {:active-state? true :blur? true}])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor colors/white-opa-10})))

  (h/test "on-press event is called"
    (let [on-press (h/mock-fn)]
      (h/render [address/view {:on-press on-press}])
      (h/fire-event :on-press (h/get-by-label-text :container))
      (h/was-called on-press))))

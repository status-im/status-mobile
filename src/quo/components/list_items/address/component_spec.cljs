(ns quo.components.list-items.address.component-spec
  (:require
    [quo.components.list-items.address.view :as address]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(defn- with-defaults
  ([] (with-defaults {}))
  ([props] (merge {:address "0x0ah...78b"} props)))

(h/describe "List items: address"
  (h/test "default render"
    (h/render-with-theme-provider [address/view (with-defaults)])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "on-press-in changes state to :pressed"
    (h/render-with-theme-provider [address/view (with-defaults)])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "on-press-in changes state to :pressed with blur? enabled"
    (h/render-with-theme-provider [address/view (with-defaults {:blur? true})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor colors/white-opa-5})))

  (h/test "on-press-out changes state to :active"
    (h/render-with-theme-provider [address/view (with-defaults {:active-state? true})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 10)})))

  (h/test "on-press-out changes state to :active with blur? enabled"
    (h/render-with-theme-provider [address/view (with-defaults {:active-state? true :blur? true})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor colors/white-opa-10})))

  (h/test "on-press event is called"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider [address/view (with-defaults {:on-press on-press})])
      (h/fire-event :on-press (h/get-by-label-text :container))
      (h/was-called on-press))))

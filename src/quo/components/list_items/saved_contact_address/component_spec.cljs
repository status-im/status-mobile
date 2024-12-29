(ns quo.components.list-items.saved-contact-address.component-spec
  (:require
    [quo.components.list-items.saved-contact-address.view :as saved-contact-address]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(defn- with-defaults
  ([] (with-defaults {}))
  ([props] (merge {:contact-props {:full-name "Thomas Anderson"}} props)))

(def account
  {:name                "New House"
   :address             "0x21a...49e"
   :emoji               "🍔"
   :customization-color :purple})

(h/describe "List items: saved contact address"
  (h/test "default render"
    (h/render-with-theme-provider [saved-contact-address/view (with-defaults)])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "renders account detail when passing one account"
    (h/render-with-theme-provider [saved-contact-address/view
                                   (with-defaults
                                    {:accounts (repeat 1 account)})])
    (h/is-truthy (h/query-by-label-text :account-container)))

  (h/test "renders account count when passing multiple accounts"
    (h/render-with-theme-provider [saved-contact-address/view
                                   (with-defaults
                                    {:accounts (repeat 2 account)})])
    (h/is-truthy (h/query-by-label-text :accounts-count)))

  (h/test "on-press-in changes state to :pressed"
    (h/render-with-theme-provider [saved-contact-address/view
                                   (with-defaults
                                    {:accounts (repeat 1 account)})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "on-press-out changes state to :active if active-state? is true (default value)"
    (h/render-with-theme-provider [saved-contact-address/view
                                   (with-defaults
                                    {:accounts (repeat 1 account)})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 10)})))

  (h/test "on-press-out changes state to :default if active-state? is false"
    (h/render-with-theme-provider [saved-contact-address/view
                                   (with-defaults
                                    {:accounts      (repeat 1 account)
                                     :active-state? false})])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/fire-event :on-press-out (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor :transparent})))

  (h/test "on-press calls on-press"
    (let [on-press (h/mock-fn)]
      (h/render-with-theme-provider [saved-contact-address/view
                                     (with-defaults
                                      {:on-press on-press
                                       :accounts (repeat 1 account)})])
      (h/fire-event :on-press (h/get-by-label-text :container))
      (h/was-called on-press))))

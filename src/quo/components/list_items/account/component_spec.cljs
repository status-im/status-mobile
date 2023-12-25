(ns quo.components.list-items.account.component-spec
  (:require
    [quo.components.list-items.account.view :as account]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(h/describe "List items: account"
  (h/test "default render'"
    (h/render [account/view])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "on-press-in changes state to :pressed"
    (h/render [account/view])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor (colors/resolve-color :blue :light 5)})))

  (h/test "on-press-in changes state to :pressed with blur? enabled"
    (h/render [account/view {:blur? true}])
    (h/fire-event :on-press-in (h/get-by-label-text :container))
    (h/wait-for #(h/has-style (h/query-by-label-text :container)
                              {:backgroundColor colors/white-opa-5})))

  (h/test "render with state :active"
    (h/render [account/view {:state :active}])
    (h/has-style (h/query-by-label-text :container)
                 {:backgroundColor (colors/resolve-color :blue :light 10)}))

  (h/test "render with state :active and blur? enabled"
    (h/render [account/view
               {:blur? true
                :state :active}])
    (h/has-style (h/query-by-label-text :container)
                 {:backgroundColor colors/white-opa-10}))

  (h/test "render with state :selected"
    (h/render [account/view {:state :selected}])
    (h/is-truthy (h/query-by-label-text :check-icon)))

  (h/test "calls on-press"
    (let [on-press (h/mock-fn)]
      (h/render [account/view {:on-press on-press}])
      (h/fire-event :on-press (h/get-by-label-text :container))
      (h/was-called on-press)))

  (h/test "renders token props if type :tag"
    (h/render [account/view {:type :tag}])
    (h/is-truthy (h/query-by-label-text :tag-container)))

  (h/test "renders keycard icon if title-icon? is true"
    (h/render [account/view {:title-icon? true}])
    (h/is-truthy (h/query-by-label-text :title-icon)))

  (h/test "doesn't render keycard icon if title-icon? is false"
    (h/render [account/view])
    (h/is-falsy (h/query-by-label-text :title-icon)))

  (h/test "renders balance container but not arrow icon if type :balance-neutral"
    (h/render [account/view {:type :balance-neutral}])
    (h/is-truthy (h/query-by-label-text :balance-container))
    (h/is-falsy (h/query-by-label-text :arrow-icon)))

  (h/test "renders balance container and negative arrow icon if type :balance-negative"
    (h/render [account/view {:type :balance-negative}])
    (h/is-truthy (h/query-by-label-text :balance-container))
    (h/is-truthy (h/query-by-label-text :icon-negative))
    (h/is-falsy (h/query-by-label-text :icon-positive)))

  (h/test "renders balance container and positive arrow icon if type :balance-positive"
    (h/render [account/view {:type :balance-positive}])
    (h/is-truthy (h/query-by-label-text :balance-container))
    (h/is-falsy (h/query-by-label-text :icon-negative))
    (h/is-truthy (h/query-by-label-text :icon-positive)))

  (h/test "renders options button if type :action"
    (let [on-options-press (h/mock-fn)]
      (h/render [account/view
                 {:type             :action
                  :on-options-press on-options-press}])
      (h/is-truthy (h/query-by-label-text :options-button))
      (h/fire-event :on-press (h/get-by-label-text :options-button))
      (h/was-called on-options-press))))

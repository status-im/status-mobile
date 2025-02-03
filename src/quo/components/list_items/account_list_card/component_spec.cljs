(ns quo.components.list-items.account-list-card.component-spec
  (:require
    [quo.components.list-items.account-list-card.view :as account-list-card]
    [test-helpers.component :as h]))

(def account-props
  {:customization-color :purple
   :size                32
   :emoji               "🍑"
   :type                :default
   :name                "Tip to Vegas"
   :address             "0x0ah...78b"})

(h/describe "List items: account list card"
  (h/test "Test icon renders for ':action :icon'"
    (h/render-with-theme-provider [account-list-card/view
                                   {:account-props    account-props
                                    :action           :icon
                                    :on-options-press (fn [])}])
    (h/is-truthy (h/get-by-label-text :icon))))

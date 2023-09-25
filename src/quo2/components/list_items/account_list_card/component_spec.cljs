(ns quo2.components.list-items.account-list-card.component-spec
  (:require
    [test-helpers.component :as h]
    [quo2.components.list-items.account-list-card.view :as account-list-card]))

(def account-props
  {:customization-color :purple
   :size                32
   :emoji               "🍑"
   :type                :default
   :name                "Tip to Vegas"
   :address             "0x0ah...78b"})

(h/describe "List items: account list card"
  (h/test "Test icon renders for ':action :icon'"
    (h/render [account-list-card/view
               {:account-props account-props
                :network       :ethereum
                :state         :default
                :action        :icon}])
    (h/is-truthy (h/get-by-label-text :icon))))

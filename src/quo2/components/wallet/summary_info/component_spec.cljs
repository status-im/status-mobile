(ns quo2.components.wallet.summary-info.component-spec
  (:require
    [quo2.components.wallet.summary-info.view :as summary-info]
    [test-helpers.component :as h]))

(def status-account-props
  {:customization-color :purple
   :size                32
   :emoji               "🍑"
   :type                :default
   :name                "Collectibles vault"
   :address             "0x0ah...78b"})

(h/describe "Wallet: Summary Info"
  (h/test "Type of `status-account` title renders"
    (h/render [summary-info/view
               {:type          :status-account
                :networks?     true
                :values        {:ethereum 150
                                :optimism 50
                                :arbitrum 25}
                :account-props status-account-props}])
    (h/is-truthy (h/get-by-text "Collectibles vault")))

  (h/test "Type of `user` title renders"
    (h/render [summary-info/view
               {:type          :user
                :networks?     true
                :values        {:ethereum 150
                                :optimism 50
                                :arbitrum 25}
                :account-props {:full-name           "M L"
                                :status-indicator?   false
                                :size                :small
                                :customization-color :blue
                                :name                "Mark Libot"
                                :address             "0x0ah...78b"
                                :status-account      (merge status-account-props {:size 16})}}])
    (h/is-truthy (h/get-by-text "Mark Libot"))
    (h/is-truthy (h/get-by-text "Collectibles vault")))

  (h/test "Networks true render"
    (h/render [summary-info/view
               {:type          :status-account
                :networks?     true
                :values        {:ethereum 150
                                :optimism 50
                                :arbitrum 25}
                :account-props status-account-props}])
    (h/is-truthy (h/get-by-label-text :networks)))

  (h/test "Networks false render"
    (h/render [summary-info/view
               {:type          :status-account
                :networks?     false
                :values        {:ethereum 150
                                :optimism 50
                                :arbitrum 25}
                :account-props status-account-props}])
    (h/is-null (h/query-by-label-text :networks))))

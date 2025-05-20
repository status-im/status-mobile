(ns quo.components.wallet.summary-info.component-spec
  (:require
    [quo.components.wallet.summary-info.view :as summary-info]
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
    (h/render-with-theme-provider [summary-info/view
                                   {:type             :status-account
                                    :networks-to-show {:ethereum {:amount       150
                                                                  :token-symbol "ETH"}
                                                       :optimism {:amount       50
                                                                  :token-symbol "ETH"}
                                                       :arbitrum {:amount       50
                                                                  :token-symbol "ETH"}}
                                    :account-props    status-account-props}])
    (h/is-truthy (h/get-by-text "Collectibles vault")))

  (h/test "Type of `user` title renders"
    (h/render-with-theme-provider [summary-info/view
                                   {:type             :user
                                    :networks-to-show {:ethereum {:amount       150
                                                                  :token-symbol "ETH"}
                                                       :optimism {:amount       50
                                                                  :token-symbol "ETH"}
                                                       :arbitrum {:amount       25
                                                                  :token-symbol "ETH"}}
                                    :account-props    {:full-name           "M L"
                                                       :status-indicator?   false
                                                       :size                :small
                                                       :customization-color :blue
                                                       :name                "Mark Libot"
                                                       :address             "0x0ah...78b"
                                                       :status-account      (merge status-account-props
                                                                                   {:size 16})}}])
    (h/is-truthy (h/get-by-text "Mark Libot"))
    (h/is-truthy (h/get-by-text "Collectibles vault")))

  (h/test "Networks specified render"
    (h/render-with-theme-provider [summary-info/view
                                   {:type             :status-account
                                    :networks-to-show {:ethereum {:amount       150
                                                                  :token-symbol "ETH"}
                                                       :optimism {:amount       50
                                                                  :token-symbol "ETH"}
                                                       :arbitrum {:amount       25
                                                                  :token-symbol "ETH"}}
                                    :account-props    status-account-props}])
    (h/is-truthy (h/get-by-label-text :networks))))

(h/describe "Wallet: network summary info"
  (h/test "Type of `network` title renders"
    (h/render-with-theme-provider [summary-info/view
                                   {:type          :network
                                    :network-props {:full-name    "Ethereum"
                                                    :network-name :ethereum}}])
    (h/is-truthy (h/get-by-text "Ethereum"))))

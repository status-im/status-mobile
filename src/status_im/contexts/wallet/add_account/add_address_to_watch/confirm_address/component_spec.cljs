(ns status-im.contexts.wallet.add-account.add-address-to-watch.confirm-address.component-spec
  (:require
    [status-im.contexts.wallet.add-account.add-address-to-watch.confirm-address.view :as confirm-address]
    [test-helpers.component :as h]))

(h/describe "Add Watch Only Account Page"
  (h/setup-restorable-re-frame)

  (h/test "Create Account button is disabled while no account name exists"
    (h/setup-subs {:wallet/watch-only-accounts        []
                   :alert-banners/top-margin          0
                   :wallet/accounts-names             #{"My account 1" "My account 2"}
                   :wallet/accounts-emojis-and-colors #{["😊" :sky] ["😶" :army]}})
    (h/render-with-context-provider
     [confirm-address/view]
     {:theme         :light
      :screen-params {:address "0xmock-address"}})
    (h/is-truthy (h/get-by-text "0xmock-address"))
    (h/is-disabled (h/get-by-label-text :confirm-button-label))))

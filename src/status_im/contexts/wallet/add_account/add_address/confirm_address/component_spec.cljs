(ns status-im.contexts.wallet.add-account.add-address.confirm-address.component-spec
  (:require
    [status-im.contexts.wallet.add-account.add-address.confirm-address.view :as confirm-address]
    [test-helpers.component :as h]))

(h/describe "Add Watch Only Account Page"
  (h/setup-restorable-re-frame)

  (h/test "Create Account button is disabled while no account name exists"
    (h/setup-subs {:wallet/watch-only-accounts []
                   :get-screen-params          {:address "0xmock-address"
                                                :title :t/add-address-to-watch
                                                :description :t/enter-eth
                                                :input-title :t/eth-or-ens
                                                :confirm-screen :screen/wallet.confirm-address-to-save
                                                :confirm-screen-props {:button-label :t/add-watched-address
                                                                       :address-type :t/watched-address
                                                                       :placeholder
                                                                       :t/default-watched-address-placeholder}
                                                :adding-address-purpose :watch}})
    (h/render [confirm-address/view])
    (h/is-truthy (h/get-by-text "0xmock-address"))
    (h/is-disabled (h/get-by-label-text :confirm-button-label))))

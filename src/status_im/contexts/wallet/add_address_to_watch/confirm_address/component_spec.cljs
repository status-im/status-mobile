(ns status-im.contexts.wallet.add-address-to-watch.confirm-address.component-spec
  (:require
    [status-im.contexts.wallet.add-address-to-watch.confirm-address.view :as confirm-address]
    [test-helpers.component :as h]
    [utils.re-frame :as rf]))

(h/describe "Add Watch Only Account Page"
  (h/setup-restorable-re-frame)

  (h/test "Create Account button is disabled while no account name exists"
    (let [callback (h/mock-fn)]
      (with-redefs [rf/dispatch #(callback)]
        (h/setup-subs {:profile/wallet-accounts []
                       :get-screen-params       {:address "0xmock-address"}})
        (h/render [confirm-address/view {}])
        (h/is-truthy (h/get-by-text "0xmock-address"))
        (h/was-not-called callback)
        (h/fire-event :change-text (h/get-by-label-text :profile-title-input) "NAME")
        (h/fire-event :press (h/get-by-translation-text :t/add-watched-address))
        (h/was-called callback)))))

(ns quo.components.wallet.account-permissions.component-spec
  (:require
    [quo.components.wallet.account-permissions.view :as account-permissions]
    [test-helpers.component :as h]
    [utils.address :as utils]))

(defn- render
  [component]
  (h/render-with-theme-provider component :light))

(def ^:private account
  {:name                "Trip to Vegas"
   :address             "0x2f0fbf0a93c5999e9b4410848403a02b38983eb2"
   :emoji               "😊"
   :customization-color :blue})

(h/describe "Wallet: Account Permissions"
  (h/test "basic render"
    (render [account-permissions/view
             {:account account}])
    (h/is-truthy (h/get-by-label-text :wallet-account-permissions)))

  (h/test "render with correct account name, emoji and address"
    (render [account-permissions/view
             {:account account}])
    (h/is-truthy (h/get-by-text (:name account)))
    (h/is-truthy (h/get-by-text (:emoji account)))
    (h/is-truthy (h/get-by-text (utils/get-short-wallet-address (:address account)))))

  (h/test "render without keycard"
    (render [account-permissions/view
             {:account account}])
    (h/is-falsy (h/query-by-label-text :wallet-account-permissions-keycard)))

  (h/test "render with keycard"
    (render [account-permissions/view
             {:account  account
              :keycard? true}])
    (h/is-truthy (h/get-by-label-text :wallet-account-permissions-keycard)))

  (h/test "render with token details"
    (render [account-permissions/view
             {:account       account
              :token-details [{:token  "SNT"
                               :amount "100"}]}])
    (h/is-truthy (h/get-by-text "100 SNT")))

  (h/test "render with multiple token details"
    (render [account-permissions/view
             {:account       account
              :token-details [{:token  "SNT"
                               :amount "100"}
                              {:token  "ETH"
                               :amount "18"}
                              {:token  "BTM"
                               :amount "1000"}]}])
    (h/is-truthy (h/get-by-text "100 SNT"))
    (h/is-truthy (h/get-by-text "18 ETH"))
    (h/is-truthy (h/get-by-text "1000 BTM")))

  (h/test "render with no relevant tokens"
    (render [account-permissions/view
             {:account       account
              :token-details []}])
    (h/is-truthy (h/get-by-translation-text :t/no-relevant-tokens)))

  (h/test "render checked? & on-change"
    (let [mock-on-change (h/mock-fn)]
      (render [account-permissions/view
               {:account   account
                :on-change mock-on-change
                :checked?  true}])
      (h/fire-event :press (h/get-by-label-text :checkbox-on))
      (h/was-called-with mock-on-change false))))

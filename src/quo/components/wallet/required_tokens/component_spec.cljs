(ns quo.components.wallet.required-tokens.component-spec
  (:require
    [quo.components.wallet.required-tokens.view :as required-tokens]
    [test-helpers.component :as h]))

(defn render
  [component]
  (h/render-with-theme-provider component :light))

(h/describe "Wallet: Required Tokens"
  (h/test "basic render"
    (render [required-tokens/view
             {:token  "SNT"
              :type   :token
              :amount 100}])
    (h/is-truthy (h/get-by-label-text :wallet-required-tokens)))

  (h/test "render collectible"
    (render [required-tokens/view
             {:type                :collectible
              :collectible-img-src (js/require "../resources/images/mock2/collectible.png")
              :collectible-name    "Diamond"}])
    (h/is-truthy (h/get-by-text "Diamond")))

  (h/test "render token with correct amount & symbol"
    (render [required-tokens/view
             {:type   :token
              :token  "ETH"
              :amount 100}])
    (h/is-truthy (h/get-by-text "100 ETH"))))

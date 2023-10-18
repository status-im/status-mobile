(ns quo.components.browser.dapp-favorites.component-spec
  (:require [quo.components.browser.dapp-favorites.view :as dapp-favorites]
            [quo.foundations.resources :as quo.resources]
            [test-helpers.component :as h]))

(def dapps
  [{:logo (quo.resources/get-dapp :coingecko) :name "CoinGecko"}
   {:logo (quo.resources/get-dapp :aave) :name "Aave"}
   {:logo (quo.resources/get-dapp :1inch) :name "1inch"}
   {:logo (quo.resources/get-dapp :zapper) :name "Zapper"}
   {:logo (quo.resources/get-dapp :uniswap) :name "Uniswap"}
   {:logo (quo.resources/get-dapp :zerion) :name "Zerion"}])

(h/describe "dapp-favorites"
  (h/test "default render"
    (let [theme :light
          props {:dapps dapps}]
      (h/render-with-theme-provider [dapp-favorites/view props] theme)
      (-> (h/expect (h/get-all-by-test-id "dapp-item"))
          (.toHaveLength 6)))))

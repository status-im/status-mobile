(ns status-im2.contexts.quo-preview.browser.dapp-favorites
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as quo.resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def dapp-favorites
  [{:logo (quo.resources/get-dapp :coingecko) :name "CoinGecko"}
   {:logo (quo.resources/get-dapp :aave) :name "Aave"}
   {:logo (quo.resources/get-dapp :1inch) :name "1inch"}
   {:logo (quo.resources/get-dapp :zapper) :name "Zapper"}
   {:logo (quo.resources/get-dapp :uniswap) :name "Uniswap"}
   {:logo (quo.resources/get-dapp :zerion) :name "Zerion"}])

(defn view
  []
  [preview/preview-container {:flex 1}
   [quo/dapp-favorites {:dapps dapp-favorites}]])

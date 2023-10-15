(ns status-im2.contexts.quo-preview.browser.dapp-item
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as quo.resources]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def coingecko {:logo (quo.resources/get-dapp :coingecko) :name "CoinGecko"})
(def aave {:logo (quo.resources/get-dapp :aave) :name "Aave"})
(def oneInch {:logo (quo.resources/get-dapp :1inch) :name "1inch"})
(def zapper {:logo (quo.resources/get-dapp :zapper) :name "Zapper"})
(def uniswap {:logo (quo.resources/get-dapp :uniswap) :name "Uniswap"})
(def zerion {:logo (quo.resources/get-dapp :zerion) :name "Zerion"})

(def descriptor
  [{:key     :dapp
    :type    :select
    :options [{:key   :coingecko
               :value "CoinGecko"}
              {:key   :aave
               :value "Aave"}
              {:key   :1inch
               :value "1inch"}
              {:key   :zapper
               :value "Zapper"}
              {:key   :uniswap
               :value "Uniswap"}
              {:key   :zerion
               :value "Zerion"}]}])

(defn view
  []
  (let [state (reagent/atom {:dapp :aave})
        dapp  (reagent/cursor state [:dapp])]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/dapp-item
        (case @dapp
          :coingecko coingecko
          :aave      aave
          :1inch     oneInch
          :zapper    zapper
          :uniswap   uniswap
          :zerion    zerion)]])))

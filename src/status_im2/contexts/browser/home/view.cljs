(ns status-im2.contexts.browser.home.view
  (:require
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [utils.i18n :as i18n]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [status-im2.common.home.top-nav.view :as common.top-nav]
    [status-im2.common.home.title-column.view :as common.title-column]
    [status-im2.contexts.browser.home.style :as style]
    [utils.re-frame :as rf]))

(def dapp-favorites
  [{:logo (quo.resources/get-dapp :coingecko) :name "CoinGecko"}
   {:logo (quo.resources/get-dapp :aave) :name "Aave"}
   {:logo (quo.resources/get-dapp :1inch) :name "1inch"}
   {:logo (quo.resources/get-dapp :zapper) :name "Zapper"}
   {:logo (quo.resources/get-dapp :uniswap) :name "Uniswap"}
   {:logo (quo.resources/get-dapp :zerion) :name "Zerion"}])

(defn view
  []
  (let [top                 (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])]
    (fn []
      [rn/view
       {:style {:margin-top top
                :flex       1}}
       [common.top-nav/view]
       [common.title-column/view
        {:label               (i18n/label :t/browser)
         :customization-color customization-color}]
       [quo/dapp-favorites {:dapps dapp-favorites}]
       [rn/scroll-view
        {:style                   style/tabs-container
         :content-container-style style/tabs-content-container}
        [quo/new-tab
         {:customization-color customization-color}]]])))

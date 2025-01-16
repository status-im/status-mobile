(ns status-im.contexts.wallet.home.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.common.token-value.view :as token-value]
    [status-im.contexts.wallet.home.tabs.assets.style :as style]
    [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading?  (rf/sub [:wallet/home-tokens-loading?])
        {:keys [tokens]} (rf/sub [:wallet/aggregated-token-values-and-balance])
        zero-balance?    (rf/sub [:wallet/zero-balance-in-all-non-watched-accounts?])
        buy-assets       (rn/use-callback
                          (fn []
                            (rf/dispatch [:show-bottom-sheet
                                          {:content buy-token/view}])))
        receive-assets   (rn/use-callback
                          (fn []
                            (rf/dispatch [:open-modal :screen/share-shell {:initial-tab :wallet}])))]
    [:<>
     (when (and (some? tokens-loading?) (not tokens-loading?) zero-balance?)
       [rn/view
        {:style style/buy-and-receive-cta-container}
        [quo/wallet-card
         {:image           (resources/get-image :buy)
          :title           (i18n/label :t/ways-to-buy)
          :subtitle        (i18n/label :t/via-card-or-bank)
          :container-style (assoc style/cta-card :margin-right 12)
          :on-press        buy-assets}]
        [quo/wallet-card
         {:image           (resources/get-image :receive)
          :title           (i18n/label :t/receive)
          :subtitle        (i18n/label :t/deposit-to-your-wallet)
          :container-style style/cta-card
          :on-press        receive-assets}]])
     (if tokens-loading?
       [quo/skeleton-list
        {:content       :assets
         :parent-height 560
         :animated?     false}]
       [rn/flat-list
        {:render-fn               token-value/view
         :data                    tokens
         :render-data             {:entry-point :wallet-stack}
         :content-container-style style/list-container}])]))

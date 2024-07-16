(ns status-im.contexts.wallet.swap.select-asset-to-pay.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.asset-list.view :as asset-list]
    [status-im.contexts.wallet.swap.select-asset-to-pay.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- search-input
  [search-text on-change-text]
  [rn/view {:style style/search-input-container}
   [quo/input
    {:small?         true
     :placeholder    (i18n/label :t/search-assets)
     :icon-name      :i/search
     :value          search-text
     :on-change-text on-change-text}]])

(def dummy-swap-proposal
  {:from           {:chain-id               1
                    :native-currency-symbol "ETH"}
   :to             {:chain-id               1
                    :native-currency-symbol "ETH"}
   :gas-amount     "23487"
   :gas-fees       {:base-fee                 "32.325296406"
                    :max-priority-fee-per-gas "0.011000001"
                    :eip1559-enabled          true}
   :estimated-time 3
   :receive-amount 99.98
   :receive-token  {:symbol  "SNT"
                    :address "0x432492384728934239789"}})

(defn- assets-view
  [search-text on-change-text]
  (let [on-token-press (fn [token]
                         (let [token-networks (:networks token)]
                           (rf/dispatch [:wallet.swap/select-asset-to-pay
                                         {:token    token
                                          :network  (when (= (count token-networks) 1)
                                                      (first token-networks))
                                          :stack-id :screen/wallet.swap-select-asset-to-pay}])
                           (rf/dispatch [:wallet.swap/select-asset-to-receive {:token token}])
                           (rf/dispatch [:wallet.swap/set-pay-amount 100])
                           (rf/dispatch [:wallet.swap/set-swap-proposal dummy-swap-proposal])
                           (rf/dispatch [:wallet.swap/set-provider])))]
    [:<>
     [search-input search-text on-change-text]
     [asset-list/view
      {:search-text    search-text
       :on-token-press on-token-press}]]))

(defn view
  []
  (let [[search-text set-search-text] (rn/use-state "")
        on-change-text                #(set-search-text %)
        on-close                      (fn []
                                        (rf/dispatch [:wallet.swap/clean-asset-to-pay])
                                        (rf/dispatch [:navigate-back]))]
    [rn/safe-area-view {:style style/container}
     [account-switcher/view
      {:on-press      on-close
       :switcher-type :select-account}]
     [quo/page-top
      {:title                     (i18n/label :t/select-asset-to-pay)
       :title-accessibility-label :title-label}]
     [assets-view search-text on-change-text]]))

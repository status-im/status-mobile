(ns status-im.contexts.wallet.send.from.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.send.from.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-press
  [address]
  (rf/dispatch [:wallet/select-from-account
                {:address  address
                 :stack-id :screen/wallet.select-from}]))

(defn view
  []
  (let [on-close (fn []
                   (rf/dispatch [:navigate-back]))
        accounts (rf/sub [:wallet/account-cards-data])]
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [account-switcher/view
                                 {:on-press      on-close
                                  :margin-top    (safe-area/get-top)
                                  :switcher-type :select-account}]}

     [quo/page-top
      {:title                     (i18n/label :t/from-label)
       :title-accessibility-label :title-label}]
     [rn/flat-list
      {:style                             style/accounts-list
       :content-container-style           style/accounts-list-container
       :data                              accounts
       :render-fn                         (fn [item]
                                            [quo/account-item
                                             {:type          :balance-neutral
                                              :on-press      #(on-press (:address item))
                                              :token-props   {:symbol "SNT"
                                                              :value  "1,000"}
                                              :balance-props {:crypto-value      "0.00"
                                                              :fiat-value        "€0.00"
                                                              :percentage-change "0.0"
                                                              :fiat-change       "€0.00"}
                                              :title-icon?   (:watch-only? item)
                                              :state         (if (:watch-only? item) :disabled :default)
                                              :title-icon    (when (:watch-only? item) :i/reveal)
                                              :account-props item}])
       :shows-horizontal-scroll-indicator false}]]))

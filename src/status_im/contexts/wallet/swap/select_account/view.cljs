(ns status-im.contexts.wallet.swap.select-account.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.swap.select-account.style :as style]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn- on-account-press
  [account]
  (rf/dispatch [:wallet.swap/start-from-account account]))

(defn- render-fn
  [item _ _]
  (let [has-balance (money/above-zero? (:asset-pay-balance item))]
    [quo/account-item
     {:type          (if has-balance :tag :default)
      :on-press      #(on-account-press item)
      :state         (if has-balance :default :disabled)
      :token-props   {:symbol (:asset-pay-symbol item)
                      :value  (:asset-pay-balance item)}
      :account-props (assoc item
                            :address       (:formatted-address item)
                            :full-address? true)}]))

(defn- on-close
  []
  (rf/dispatch [:wallet/clean-current-viewing-account])
  (events-helper/navigate-back))

(defn view
  []
  (let [accounts (rf/sub [:wallet/accounts-with-balances])]
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [quo/page-nav
                                 {:margin-top (safe-area/get-top)
                                  :icon-name  :i/close
                                  :on-press   on-close}]}
     [quo/page-top
      {:title                     (i18n/label :t/from-label)
       :title-accessibility-label :title-label}]
     [rn/flat-list
      {:style                             style/accounts-list
       :content-container-style           style/accounts-list-container
       :data                              accounts
       :render-fn                         render-fn
       :shows-horizontal-scroll-indicator false}]]))

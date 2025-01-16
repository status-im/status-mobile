(ns status-im.contexts.wallet.send.from.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.send.from.style :as style]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn- on-account-press
  [address network-details]
  (rf/dispatch [:wallet/select-from-account
                {:address         address
                 :network-details network-details
                 :stack-id        :screen/wallet.select-from
                 :start-flow?     true}]))

(defn- render-fn
  [item _ _ {:keys [network-details]}]
  (let [has-balance (money/above-zero? (string/replace-first (:asset-pay-balance item) "<" ""))]
    [quo/account-item
     {:type          (if has-balance :tag :default)
      :on-press      #(on-account-press (:address item) network-details)
      :state         (if has-balance :default :disabled)
      :token-props   {:symbol (:asset-pay-symbol item)
                      :value  (:asset-pay-balance item)}
      :account-props (assoc item
                            :address       (:formatted-address item)
                            :full-address? true)}]))

(defn view
  []
  (let [token-symbol    (rf/sub [:wallet/send-token-symbol])
        token           (rf/sub [:wallet/token-by-symbol-from-first-available-account-with-balance
                                 token-symbol])
        accounts        (rf/sub [:wallet/accounts-with-balances token])
        network-details (rf/sub [:wallet/network-details])]
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [account-switcher/view
                                 {:on-press      #(rf/dispatch [:navigate-back])
                                  :margin-top    (safe-area/get-top)
                                  :switcher-type :select-account}]}
     [quo/page-top
      {:title                     (i18n/label :t/from-label)
       :title-accessibility-label :title-label}]
     [rn/flat-list
      {:style                             style/accounts-list
       :content-container-style           style/accounts-list-container
       :data                              accounts
       :render-data                       {:network-details network-details}
       :render-fn                         render-fn
       :shows-horizontal-scroll-indicator false}]]))

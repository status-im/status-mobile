(ns status-im.contexts.wallet.send.from.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.collectible.utils :as collectible-utils]
    [status-im.contexts.wallet.send.from.style :as style]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(defn- on-account-press
  [address general-flow? collectible-tx?]
  (when general-flow?
    (rf/dispatch [:wallet/clean-selected-token])
    (rf/dispatch [:wallet/clean-selected-collectible]))
  (rf/dispatch [:wallet/select-from-account
                {:address     address
                 :stack-id    :screen/wallet.select-from
                 :start-flow? (not (or general-flow? collectible-tx?))}]))

(defn- on-close
  []
  (rf/dispatch [:wallet/clean-selected-collectible {:ignore-entry-point? true}])
  (rf/dispatch [:wallet/clean-current-viewing-account]))

(defn- render-fn
  [item _ _ {:keys [general-flow? collectible-tx? collectible]}]
  (let [account-address (:address item)
        balance         (cond
                          general-flow?   0
                          collectible-tx? (collectible-utils/collectible-balance collectible
                                                                                 account-address)
                          :else           (string/replace-first (:asset-pay-balance item) "<" ""))
        has-balance?    (money/above-zero? balance)
        asset-symbol    (if collectible-tx? "" (:asset-pay-symbol item))
        asset-value     (if collectible-tx? (str balance) (:asset-pay-balance item))]
    [quo/account-item
     {:type          (if has-balance? :tag :default)
      :on-press      #(on-account-press account-address general-flow? collectible-tx?)
      :state         (if (or has-balance? general-flow?) :default :disabled)
      :token-props   (when-not general-flow?
                       {:symbol asset-symbol
                        :value  asset-value})
      :account-props item}]))

(defn view
  []
  (let [general-flow?   (rf/sub [:wallet/send-general-flow?])
        collectible-tx? (rf/sub [:wallet/send-tx-type-collectible?])
        token-symbol    (rf/sub [:wallet/send-token-symbol])
        token           (rf/sub [:wallet/token-by-symbol-from-first-available-account-with-balance
                                 token-symbol])
        collectible     (rf/sub [:wallet/wallet-send-collectible])
        accounts        (if (or general-flow? collectible-tx?)
                          (rf/sub [:wallet/operable-accounts])
                          (rf/sub [:wallet/accounts-with-balances token]))]
    (hot-reload/use-safe-unmount on-close)
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [quo/page-nav
                                 {:type       :no-title
                                  :icon-name  :i/close
                                  :on-press   events-helper/navigate-back
                                  :margin-top (safe-area/get-top)
                                  :background :blur}]}
     [quo/page-top
      {:title                     (i18n/label :t/from-label)
       :title-accessibility-label :title-label}]
     [rn/flat-list
      {:style                             style/accounts-list
       :content-container-style           style/accounts-list-container
       :data                              accounts
       :render-data                       {:general-flow?   general-flow?
                                           :collectible-tx? collectible-tx?
                                           :collectible     collectible}
       :render-fn                         render-fn
       :shows-horizontal-scroll-indicator false}]]))

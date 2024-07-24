(ns status-im.contexts.wallet.send.from.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.send.from.style :as style]
    [status-im.setup.hot-reload :as hot-reload]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-account-press
  [address network-details]
  (rf/dispatch [:wallet/select-from-account
                {:address         address
                 :network-details network-details
                 :stack-id        :screen/wallet.select-from}]))

(defn- on-close
  []
  (rf/dispatch [:wallet/clean-current-viewing-account]))

(defn- render-fn
  [item _ _ {:keys [network-details]}]
  (let [transformed-address (rf/sub [:wallet/account-address (:address item)
                                     (:network-preferences-names item)])]
    [quo/account-item
     {:on-press      #(on-account-press (:address item) network-details)
      :account-props (assoc item
                            :address       transformed-address
                            :full-address? true)}]))

(defn view
  []
  (let [accounts        (rf/sub [:wallet/accounts-with-current-asset])
        network-details (rf/sub [:wallet/network-details])]
    (hot-reload/use-safe-unmount on-close)
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

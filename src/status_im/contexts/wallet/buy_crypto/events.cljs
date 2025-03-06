(ns status-im.contexts.wallet.buy-crypto.events
  (:require [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im.contexts.wallet.sheets.buy-network-selection.view :as buy-network-selection]
            [status-im.contexts.wallet.sheets.select-asset.view :as select-asset]
            [utils.i18n :as i18n]))

(rf/reg-event-fx :wallet.buy-crypto/select-provider
 (fn [{:keys [db]} [{:keys [account provider recurrent?]}]]
   (let [swap-network (get-in db [:wallet :ui :swap :network])]
     {:db (-> db
              (assoc-in [:wallet :ui :buy-crypto :account] account)
              (assoc-in [:wallet :ui :buy-crypto :provider] provider)
              (assoc-in [:wallet :ui :buy-crypto :recurrent?] recurrent?))
      :fx [(if swap-network
             [:dispatch [:wallet.buy-crypto/select-network swap-network]]
             [:dispatch
              [:show-bottom-sheet
               {:content
                (fn []
                  [buy-network-selection/view
                   {:provider provider
                    :account  account}])}]])]})))

(rf/reg-event-fx :wallet.buy-crypto/select-network
 (fn [{:keys [db]} [network]]
   (let [provider                    (get-in db [:wallet :ui :buy-crypto :provider])
         network-chain-id            (:chain-id network)
         supported-tokens            (:supported-tokens provider)
         supported-tokens-on-network (set (map :symbol
                                               (filter (fn [token]
                                                         (= (:chain-id token) network-chain-id))
                                                       supported-tokens)))]
     {:db (assoc-in db [:wallet :ui :buy-crypto :network] network)
      :fx [[:dispatch
            [:show-bottom-sheet
             {:content
              (fn []
                [select-asset/view
                 {:title         (i18n/label :t/select-asset-to-buy)
                  :network       network
                  :provider      provider
                  :on-select     (fn [token]
                                   (rf/dispatch [:wallet/get-crypto-on-ramp-url {:token token}]))
                  :hide-token-fn (fn [_ token]
                                   (not (contains? supported-tokens-on-network
                                                   (:symbol token))))}])}]]]})))

(rf/reg-event-fx
 :wallet/get-crypto-on-ramp-url
 (fn [{:keys [db]} [{:keys [token]}]]
   (let [account     (get-in db [:wallet :ui :buy-crypto :account])
         provider    (get-in db [:wallet :ui :buy-crypto :provider])
         network     (get-in db [:wallet :ui :buy-crypto :network])
         recurrent?  (get-in db [:wallet :ui :buy-crypto :recurrent?])
         provider-id (:id provider)
         parameters  {:symbol      (:symbol token)
                      :destAddress (:address account)
                      :chainID     (:chain-id network)
                      :isRecurrent recurrent?}]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getCryptoOnRampURL"
              :params     [provider-id parameters]
              :on-success (fn [url]
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch [:wallet.buy-crypto/clean-all])
                            (rn/open-url url))
              :on-error   [:wallet/log-rpc-error {:event :wallet/get-crypto-on-ramps}]}]]]})))

(rf/reg-event-fx :wallet.buy-crypto/clean-all
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :buy-crypto)}))

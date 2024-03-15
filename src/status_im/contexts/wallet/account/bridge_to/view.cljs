(ns status-im.contexts.wallet.account.bridge-to.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.bridge-to.style :as style]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- bridge-token-component
  []
  (fn [{:keys [chain-id network-name]} token]
    (let [network           (rf/sub [:wallet/network-details-by-chain-id chain-id])
          currency          (rf/sub [:profile/currency])
          currency-symbol   (rf/sub [:profile/currency-symbol])
          all-balances      (:balances-per-chain token)
          balance-for-chain (utils/get-balance-for-chain all-balances chain-id)
          crypto-formatted  (or (:balance balance-for-chain) "0.00")
          fiat-value        (utils/token-fiat-value currency
                                                    (or (:balance balance-for-chain) 0)
                                                    token)
          fiat-formatted    (utils/get-standard-fiat-format crypto-formatted currency-symbol fiat-value)]
      [quo/network-list
       {:label         (name network-name)
        :network-image (quo.resources/get-network (:network-name network))
        :token-value   (str crypto-formatted " " (:symbol token))
        :fiat-value    fiat-formatted
        :on-press      #(rf/dispatch [:wallet/select-bridge-network
                                      {:network-chain-id chain-id
                                       :stack-id         :wallet-bridge}])}])))

(defn- view-internal
  []
  (let [send-bridge-data (rf/sub [:wallet/wallet-send])
        network-details  (rf/sub [:wallet/network-details])
        account          (rf/sub [:wallet/current-viewing-account])
        token            (:token send-bridge-data)
        token-symbol     (:symbol token)
        tokens           (:tokens account)
        mainnet          (first network-details)
        layer-2-networks (rest network-details)
        account-token    (some #(when (= token-symbol (:symbol %)) %) tokens)
        bridge-to-title  (i18n/label :t/bridge-to
                                     {:name (string/upper-case (str (:name token)))})]
    [rn/view
     [account-switcher/view
      {:on-press            #(rf/dispatch [:navigate-back])
       :icon-name           :i/arrow-left
       :accessibility-label :top-bar}]
     [quo/page-top {:title bridge-to-title}]
     [rn/view style/content-container
      [bridge-token-component (assoc mainnet :network-name :t/mainnet) account-token]]

     [quo/divider-label (i18n/label :t/layer-2)]
     [rn/flat-list
      {:data                    layer-2-networks
       :render-fn               (fn [network]
                                  [bridge-token-component network account-token])
       :content-container-style style/content-container}]]))

(def view (quo.theme/with-theme view-internal))

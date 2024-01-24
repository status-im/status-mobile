(ns status-im.contexts.wallet.account.bridge-to.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as quo.resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.bridge-to.style :as style]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- bridge-token-component
  []
  (fn [bridge token]
    (let [network           (rf/sub [:wallet/network-details-by-chain-id (:chain-id bridge)])
          currency          (rf/sub [:profile/currency])
          currency-symbol   (rf/sub [:profile/currency-symbol])
          all-balances      (:balances-per-chain token)
          balance-for-chain (utils/get-balance-for-chain all-balances (:chain-id bridge))
          crypto-formatted  (or (:balance balance-for-chain) "0.00")
          fiat-value        (utils/total-network-fiat-value currency
                                                            (or (:balance balance-for-chain) 0)
                                                            token)
          fiat-formatted    (utils/get-standard-fiat-format crypto-formatted currency-symbol fiat-value)]
      [quo/network-list
       {:label         (name (:network-name bridge))
        :network-image (quo.resources/get-network (:network-name network))
        :token-value   (str crypto-formatted " " (:symbol token))
        :fiat-value    fiat-formatted}])))

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
        account-token    (first (filter #(= token-symbol (% :symbol)) tokens))]

    [rn/view
     [account-switcher/view
      {:on-press            #(rf/dispatch [:navigate-back-within-stack :wallet-bridge-to])
       :icon-name           :i/arrow-left
       :accessibility-label :top-bar}]
     [quo/text-combinations
      {:container-style style/header-container
       :title           (i18n/label :t/bridge-to {:name (string/upper-case (str (:label token)))})}]
     [rn/view style/content-container
      [bridge-token-component (assoc mainnet :network-name "Mainnet") account-token]]
     [quo/divider-line {:container-style style/divider-line-style}]
     [rn/view {:style style/layer-two-wrapper}
      [quo/text
       {:style           {:color colors/neutral-50}
        :size            :paragraph-2
        :number-of-lines 1}
       (i18n/label :t/layer-2)]]
     [rn/flat-list
      {:data                    layer-2-networks
       :render-fn               (fn [network]
                                  [bridge-token-component network account-token])
       :content-container-style style/content-container}]]))

(def view (quo.theme/with-theme view-internal))

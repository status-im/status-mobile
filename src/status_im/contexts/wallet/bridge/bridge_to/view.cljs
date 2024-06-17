(ns status-im.contexts.wallet.bridge.bridge-to.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.contexts.wallet.bridge.bridge-to.style :as style]
    [status-im.contexts.wallet.common.account-switcher.view :as account-switcher]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- bridge-token-component
  []
  (fn [{:keys [chain-id network-name]} {:keys [networks] :as token}]
    (let [network                     (rf/sub [:wallet/network-details-by-chain-id chain-id])
          currency                    (rf/sub [:profile/currency])
          currency-symbol             (rf/sub [:profile/currency-symbol])
          balance                     (utils/calculate-total-token-balance token [chain-id])
          crypto-value                (utils/get-standard-crypto-format token balance)
          fiat-value                  (utils/calculate-token-fiat-value
                                       {:currency currency
                                        :balance  balance
                                        :token    token})
          fiat-formatted              (utils/get-standard-fiat-format crypto-value
                                                                      currency-symbol
                                                                      fiat-value)
          token-available-on-network? (network-utils/token-available-on-network? networks chain-id)]
      [quo/network-list
       {:label         (name network-name)
        :network-image (quo.resources/get-network (:network-name network))
        :token-value   (str crypto-value " " (:symbol token))
        :fiat-value    fiat-formatted
        :state         (if token-available-on-network? :default :disabled)
        :on-press      #(rf/dispatch [:wallet/select-bridge-network
                                      {:network-chain-id chain-id
                                       :stack-id         :screen/wallet.bridge-to}])}])))

(defn view
  []
  (let [network-details  (rf/sub [:wallet/network-details])
        account          (rf/sub [:wallet/current-viewing-account])
        token            (rf/sub [:wallet/wallet-send-token])
        token-symbol     (:symbol token)
        tokens           (:tokens account)
        mainnet          (first network-details)
        layer-2-networks (rest network-details)
        account-token    (some #(when (= token-symbol (:symbol %)) %) tokens)
        account-token    (when account-token (assoc account-token :networks (:networks token)))
        bridge-to-title  (i18n/label :t/bridge-to
                                     {:name (string/upper-case (str token-symbol))})]

    (rn/use-unmount #(rf/dispatch [:wallet/clean-bridge-to-selection]))

    [rn/view
     [account-switcher/view
      {:on-press            #(rf/dispatch [:navigate-back])
       :icon-name           :i/arrow-left
       :accessibility-label :top-bar
       :switcher-type       :select-account}]
     [quo/page-top {:title bridge-to-title}]
     [rn/view style/content-container
      [bridge-token-component (assoc mainnet :network-name :t/mainnet) account-token]]

     [quo/divider-label (i18n/label :t/layer-2)]
     [rn/flat-list
      {:data                    layer-2-networks
       :render-fn               (fn [network]
                                  [bridge-token-component network account-token])
       :content-container-style style/content-container}]]))

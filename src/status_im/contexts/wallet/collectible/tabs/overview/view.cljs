(ns status-im.contexts.wallet.collectible.tabs.overview.view
  (:require
    [clojure.string :as string]
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im.contexts.wallet.collectible.tabs.overview.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- trait-item
  [{:keys [trait-type value]}]
  [quo/data-item
   {:subtitle-type   :default
    :card?           true
    :status          :default
    :size            :default
    :title           trait-type
    :subtitle        value
    :container-style style/traits-item}])

(defn- traits-section
  [traits]
  [rn/view
   [quo/section-label
    {:section         (i18n/label :t/traits)
     :container-style style/traits-title-container}]
   [rn/flat-list
    {:render-fn               trait-item
     :data                    traits
     :key                     :collectibles-list
     :key-fn                  :id
     :num-columns             2
     :content-container-style style/traits-container}]])

(defn- info
  [{:keys [chain-id account]}]
  (let [{:keys [network-name]} (rf/sub [:wallet/network-details-by-chain-id chain-id])
        subtitle               (some-> network-name
                                       name
                                       string/capitalize)]
    [rn/view {:style style/info-container}
     [rn/view {:style style/account}
      [quo/data-item
       {:card?               true
        :status              :default
        :size                :default
        :title               (i18n/label :t/account-title)
        :subtitle-type       :account
        :subtitle            (:name account)
        :emoji               (:emoji account)
        :customization-color (:color account)}]]
     [rn/view {:style style/network}
      [quo/data-item
       {:subtitle-type :network
        :card?         true
        :status        :default
        :size          :default
        :title         (i18n/label :t/network)
        :network-image (quo.resources/get-network network-name)
        :subtitle      subtitle}]]]))

(defn view
  [collectible]
  (let [owner-address (or (rf/sub [:wallet/current-viewing-account-address])
                          (-> collectible :ownership first :address))
        owner-account (rf/sub [:wallet-connect/account-details-by-address owner-address])
        traits        (-> collectible :collectible-data :traits)]
    [:<>
     [info
      {:chain-id (-> collectible :id :contract-id :chain-id)
       :account  owner-account}]
     (when (pos? (count traits))
       [traits-section traits])]))

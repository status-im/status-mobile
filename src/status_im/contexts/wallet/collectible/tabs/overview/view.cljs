(ns status-im.contexts.wallet.collectible.tabs.overview.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [quo.theme]
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
  []
  (let [traits (rf/sub [:wallet/last-collectible-details-traits])]
    (when (pos? (count traits))
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
         :content-container-style style/traits-container}]])))

(defn- info
  []
  (let [chain-id                   (rf/sub [:wallet/last-collectible-details-chain-id])
        {:keys [network-name]}     (rf/sub [:wallet/network-details-by-chain-id chain-id])
        subtitle                   (string/capitalize (name (or network-name "")))
        {:keys [name emoji color]} (rf/sub [:wallet/last-collectible-details-owner])]
    [rn/view {:style style/info-container}
     [rn/view {:style style/account}
      [quo/data-item
       {:card?               true
        :status              :default
        :size                :default
        :title               (i18n/label :t/account-title)
        :subtitle            name
        :emoji               emoji
        :subtitle-type       :account
        :customization-color color}]]
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
  []
  [:<>
   [info]
   [traits-section]])

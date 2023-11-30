(ns status-im2.contexts.wallet.collectible.tabs.overview.view
  (:require
   [clojure.string :as string]
   [quo.core :as quo] 
   [quo.foundations.resources :as quo.resources]
   [quo.theme]
   [react-native.core :as rn]
   [status-im2.contexts.wallet.collectible.tabs.overview.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- traits-section
  [traits]
  (when (pos? (count traits))
    [rn/view
     [quo/section-label
      {:section         (i18n/label :t/traits)
       :container-style style/traits-title-container}]
     [rn/flat-list
      {:render-fn               (fn [{:keys [trait-type value]}]
                                  [quo/data-item
                                   {:description     :default
                                    :card?           true
                                    :status          :default
                                    :size            :default
                                    :title           trait-type
                                    :subtitle        value
                                    :container-style style/traits-item}])
       :data                    traits
       :key                     :collectibles-list
       :key-fn                  :id
       :num-columns             2
       :content-container-style style/traits-container}]]))

(defn- info
  [chain-id]
  (let [network         (rf/sub [:wallet/network-details-by-chain-id
                                 chain-id])
        network-keyword (get network :network-name)
        network-name    (string/capitalize (name network-keyword))]
    [rn/view
     {:style style/info-container}
     [rn/view {:style style/account}
      [quo/data-item
       {:description         :account
        :card?               true
        :status              :default
        :size                :default
        :title               (i18n/label :t/account-title)
        :subtitle            "Collectibles vault"
        :emoji               "🎮"
        :customization-color :yellow}]]

     [rn/view {:style style/network}
      [quo/data-item
       {:description   :network
        :card?         true
        :status        :default
        :size          :default
        :title         (i18n/label :t/network)
        :network-image (quo.resources/get-network network-keyword)
        :subtitle      network-name}]]]))

(defn- view-internal
  []
  (let [collectible-details                              (rf/sub [:wallet/last-collectible-details])
        {:keys [traits id]} collectible-details
        chain-id                                         (get-in id [:contract-id :chain-id])]
  [:<>
   [info chain-id]
   [traits-section traits]]))

(def view (quo.theme/with-theme view-internal))

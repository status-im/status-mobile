(ns status-im.contexts.wallet.sheets.network-filter.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.networks.config :as networks.config]
    [status-im.contexts.wallet.networks.core :as networks]
    [status-im.contexts.wallet.sheets.network-filter.network-field :as network-field]
    [status-im.contexts.wallet.sheets.network-filter.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn on-network-press
  [chain-id]
  (rf/dispatch [:wallet/filter-network-balances {:by-id chain-id}]))

(defn render-network
  [chain-id]
  (let [customization-color                 (rf/sub [:profile/customization-color])
        {:keys [chain-id full-name source]} (rf/sub [:wallet/network-by-id chain-id])
        network-toggled?                    (rf/sub [:wallet/network-filter-toggled? chain-id])
        disabled?                           (rf/sub [:wallet/disable-network-filter? chain-id])
        network-balance                     (rf/sub [:wallet/balance-for-network-filter chain-id])
        n-collectibles                      (rf/sub [:wallet/collectibles-count-for-network-filter
                                                     chain-id])]
    [network-field/view
     {:title               full-name
      :network-toggled?    network-toggled?
      :disabled?           disabled?
      :image-source        source
      :balance             network-balance
      :n-collectibles      n-collectibles
      :on-press            (when-not disabled? #(on-network-press chain-id))
      :customization-color customization-color
      :new?                (networks/new-network? chain-id)}]))

(defn view
  []
  (let [active-chain-ids (rf/sub [:wallet/active-chain-ids])
        banner-network   (rf/sub [:wallet/network-by-id
                                  networks.config/chain-id-for-new-network-banner])]
    [rn/view {:style {:padding-horizontal 20}}
     [rn/view {:style style/header-container}
      [quo/text
       {:size   :heading-2
        :weight :semi-bold}
       (i18n/label :t/show-network-balances)]]
     [quo/item-list
      {:data            active-chain-ids
       :render-fn       render-network
       :container-style {:margin-top 12}}]
     [quo/information-box
      {:type  :default
       :icon  :i/info
       :style {:margin-top 12}}
      (i18n/label :t/new-network-info {:network (:full-name banner-network)})]
     [quo/button
      {:type            :outline
       :container-style {:margin-vertical 12}
       :size            40
       :icon-left       :i/settings
       :on-press        #(rf/dispatch [:open-modal :screen/settings.network-settings])}
      [quo/text {:weight :medium} (i18n/label :t/manage-networks)]]]))

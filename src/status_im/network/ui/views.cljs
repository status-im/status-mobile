(ns status-im.network.ui.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.network.core :as network]
            [status-im.network.ui.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.toolbar.view :as toolbar])
  (:require-macros [status-im.utils.views :as views]))

(defn- network-icon [connected? size]
  [react/view (styles/network-icon connected? size)
   [vector-icons/icon :main-icons/network {:color (if connected? :white :gray)}]])

(defn network-badge [& [{:keys [name connected?]}]]
  [react/view styles/network-badge
   [network-icon connected? 56]
   [react/view {:padding-left 16}
    [react/text {:style styles/badge-name-text}
     (or name (i18n/label :t/new-network))]
    (when connected?
      [react/i18n-text {:style styles/badge-connected-text
                        :key   :connected}])]])

(def mainnet?
  #{"mainnet" "mainnet_rpc"})

(defn navigate-to-network [network]
  (re-frame/dispatch [::network/network-entry-pressed network]))

(defn render-network [current-network]
  (fn [{:keys [id name] :as network}]
    (let [connected? (= id current-network)
          rpc?       (get-in network [:config :UpstreamConfig :Enabled] false)]
      [list/touchable-item #(navigate-to-network network)
       [react/view styles/network-item
        [network-icon connected? 40]
        [react/view {:padding-horizontal 16}
         [react/text {:style styles/network-item-name-text}
          (if rpc? name (str name " " "(LES)"))]
         (when connected?
           [react/text {:style               styles/network-item-connected-text
                        :accessibility-label :connected-text}
            (i18n/label :t/connected)])]]])))

(views/defview network-settings []
  (views/letsubs [current-network [:networks/current-network]
                  networks        [:get-networks]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/network-settings)]
      [toolbar/actions
       [(toolbar.actions/add false #(re-frame/dispatch [::network/add-network-pressed]))]]]
     [react/view styles/wrapper
      [list/section-list {:sections           [{:title (i18n/label :t/main-networks)
                                                :key :mainnet
                                                :data (:mainnet networks)}
                                               {:title (i18n/label :t/test-networks)
                                                :key :testnet
                                                :data (:testnet networks)}
                                               {:title (i18n/label :t/custom-networks)
                                                :key :custom
                                                :data (:custom networks)}]
                          :key-fn             :id
                          :default-separator? true
                          :render-fn          (render-network current-network)}]]]))

(ns status-im.ui.screens.network.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.network.core :as network]
            [status-im.ui.screens.network.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.views :as views]))

(defn- network-icon [connected? size]
  [react/view (styles/network-icon connected? size)
   [icons/icon :main-icons/network {:color (if connected? colors/white-persist colors/gray)}]])

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

(defn render-network [{:keys [id name] :as network} _ _ current-network]
  (let [connected? (= id current-network)]
    [list/touchable-item #(re-frame/dispatch [::network/network-entry-pressed network])
     [react/view styles/network-item
      [network-icon connected? 40]
      [react/view {:padding-horizontal 16}
       [react/text {:style styles/network-item-name-text} name]
       (when connected?
         [react/text {:style               styles/network-item-connected-text
                      :accessibility-label :connected-text}
          (i18n/label :t/connected)])]]]))

(views/defview network-settings []
  (views/letsubs [current-network [:networks/current-network]
                  networks        [:get-networks]]
    [react/view {:flex 1}
     [topbar/topbar {:title       (i18n/label :t/network-settings)
                     :right-accessories
                     [{:icon    :main-icons/add
                       :on-press #(re-frame/dispatch [::network/add-network-pressed])}]}]
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
                          :render-data        current-network
                          :render-fn          render-network}]]]))

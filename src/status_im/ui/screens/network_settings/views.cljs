(ns status-im.ui.screens.network-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.network-settings.styles :as styles]))

(defn- network-icon [connected? size]
  [react/view (styles/network-icon connected? size)
   [vector-icons/icon :icons/network {:color (if connected? :white :gray)}]])

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
  (re-frame/dispatch [:navigate-to :network-details {:networks/selected-network network}]))

(defn navigate-to-add-network []
  (re-frame/dispatch [:edit-network]))

(defn render-network [current-network]
  (fn [{:keys [id name] :as network}]
    (let [connected? (= id current-network)]
      [list/touchable-item #(navigate-to-network network)
       [react/view styles/network-item
        [network-icon connected? 40]
        [react/view {:padding-horizontal 16}
         [react/text {:style styles/network-item-name-text}
          name]
         (when connected?
           [react/text {:style               styles/network-item-connected-text
                        :accessibility-label :connected-text}
            (i18n/label :t/connected)])]]])))

(views/defview network-settings []
  (views/letsubs [{:keys [network]} [:get-current-account]
                  networks          [:get-networks]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/network-settings)]
      [toolbar/actions
       [(toolbar.actions/add false navigate-to-add-network)]]]
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
                          :render-fn          (render-network network)}]]]))

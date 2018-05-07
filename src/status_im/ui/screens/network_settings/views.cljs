(ns status-im.ui.screens.network-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.network-settings.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.utils.config :as config]))

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
      [react/text {:style styles/badge-connected-text}
       (i18n/label :t/connected)])]])

(def mainnet?
  #{"mainnet" "mainnet_rpc"})

(defn navigate-to-network [network]
  (re-frame/dispatch [:navigate-to :network-details {:networks/selected-network network}]))

(defn wrap-mainnet-warning [network cb]
  (fn []
    (if (and config/mainnet-warning-enabled?
             (mainnet? (:id network)))
      (utils/show-confirmation (i18n/label :t/mainnet-warning-title)
                               (i18n/label :t/mainnet-warning-text)
                               (i18n/label :t/mainnet-warning-ok-text)
                               #(cb network))
      (cb network))))

(defn render-network [current-network]
  (fn [{:keys [id name] :as network}]
    (let [connected? (= id current-network)]
      [react/touchable-highlight
       {:on-press            (wrap-mainnet-warning network navigate-to-network)
        :accessibility-label :network-item}
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
  (views/letsubs [{:keys [network networks]} [:get-current-account]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/network-settings)]
     [react/view styles/wrapper
      [list/flat-list {:data               (vals networks)
                       :key-fn             :id
                       :default-separator? true
                       :render-fn          (render-network network)}]]]))

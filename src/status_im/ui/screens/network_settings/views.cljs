(ns status-im.ui.screens.network-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.action-button.styles :as action-button-styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.screens.network-settings.styles :as styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]))

(defn network-icon [connected? size]
  [react/view (styles/network-icon connected? size)
   [vector-icons/icon :icons/network {:color (if connected? :white :gray)}]])

(defn actions-view []
  [react/view action-button-styles/actions-list])

(defn render-network [current-network]
  (fn [{:keys [id name] :as network}]
    (let [connected? (= id current-network)]
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:navigate-to :network-details {:networks/selected-network network}])
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
     [react/view {:flex 1}
      [list/flat-list {:style     styles/networks-list
                       :data      (vals networks)
                       :key-fn    :id
                       :render-fn (render-network network)
                       :header    [react/view
                                   [actions-view
                                    [common/bottom-shadow]
                                    [common/form-title (i18n/label :t/existing-networks)
                                     {:count-value (count networks)}]
                                    [common/list-header]]]
                       :footer    [react/view
                                   [common/list-footer]
                                   [common/bottom-shadow]]}]]]))

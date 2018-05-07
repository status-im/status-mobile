(ns status-im.ui.screens.offline-messaging-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.offline-messaging-settings.styles :as styles]))

(defn- wnode-icon [connected?]
  [react/view (styles/wnode-icon connected?)
   [vector-icons/icon :icons/wnode {:color (if connected? :white :gray)}]])

(defn- render-row [current-wnode]
  (fn [{:keys [name id]}]
    (let [connected? (= id current-wnode)]
      [react/touchable-highlight
       {:on-press            #(re-frame/dispatch [:connect-wnode id])
        :accessibility-label :mailserver-item}
       [react/view styles/wnode-item
        [wnode-icon connected?]
        [react/view styles/wnode-item-inner
         [react/text {:style styles/wnode-item-name-text}
          name]]]])))

(views/defview offline-messaging-settings []
  (views/letsubs [current-wnode [:settings/current-wnode]
                  wnodes        [:settings/network-wnodes]]
                 [react/view {:flex 1}
                  [status-bar/status-bar]
                  [toolbar/simple-toolbar (i18n/label :t/offline-messaging-settings)]
                  [react/view styles/wrapper
                   [list/flat-list {:data               (vals wnodes)
                                    :default-separator? false
                                    :key-fn             :id
                                    :render-fn          (render-row current-wnode)}]]]))

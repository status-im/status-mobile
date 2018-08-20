(ns status-im.ui.screens.offline-messaging-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.offline-messaging-settings.styles :as styles]))

(defn- wnode-icon [connected?]
  [react/view (if platform/desktop?
                {:style (styles/wnode-icon-container connected?)}
                (styles/wnode-icon-container connected?))
   [vector-icons/icon :icons/wnode
    (if platform/desktop? {:style (styles/wnode-icon connected?)}
        (styles/wnode-icon connected?))]])

(defn connect-to-mailserver [id]
  (re-frame/dispatch [:connect-wnode id]))

(defn navigate-to-add-mailserver [wnode-id]
  (re-frame/dispatch [:edit-mailserver wnode-id]))

(defn render-row [current-wnode-id]
  (fn [{:keys [name id user-defined]}]
    (let [connected? (= id current-wnode-id)]
      [react/touchable-highlight
       {:on-press            #(if user-defined
                                (navigate-to-add-mailserver id)
                                (connect-to-mailserver id))
        :accessibility-label :mailserver-item}
       [react/view styles/wnode-item
        [wnode-icon connected?]
        [react/view styles/wnode-item-inner
         [react/text {:style styles/wnode-item-name-text}
          name]]]])))

(views/defview offline-messaging-settings []
  (views/letsubs [current-wnode-id [:settings/current-wnode]
                  wnodes           [:settings/network-wnodes]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/offline-messaging-settings)]
      (when config/add-custom-mailservers-enabled?
        [toolbar/actions
         [(toolbar.actions/add false (partial navigate-to-add-mailserver nil))]])]
     [react/view styles/wrapper
      [list/flat-list {:data               (vals wnodes)
                       :default-separator? false
                       :key-fn             :id
                       :render-fn          (render-row current-wnode-id)}]]]))

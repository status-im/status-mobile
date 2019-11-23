(ns status-im.ui.screens.bootnodes-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.profile.components.views :as profile.components]
            [status-im.ui.screens.bootnodes-settings.styles :as styles]))

(defn navigate-to-add-bootnode [id]
  (re-frame/dispatch [:bootnodes.ui/add-bootnode-pressed id]))

(defn render-row [{:keys [name id]}]
  [react/touchable-highlight
   {:on-press            #(navigate-to-add-bootnode id)
    :accessibility-label :bootnode-item}
   [react/view styles/bootnode-item
    [react/view styles/bootnode-item-inner
     [react/text {:style styles/bootnode-item-name-text}
      name]]]])

(views/defview bootnodes-settings []
  (views/letsubs [bootnodes-enabled [:settings/bootnodes-enabled]
                  bootnodes         [:settings/network-bootnodes]]
    [react/view {:flex 1}
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/bootnodes-settings)]
      [toolbar/actions
       [(toolbar.actions/add false #(navigate-to-add-bootnode nil))]]]
     [react/view styles/switch-container
      [profile.components/settings-switch-item
       {:label-kw  :t/bootnodes-enabled
        :value     bootnodes-enabled
        :action-fn #(re-frame/dispatch [:bootnodes.ui/custom-bootnodes-switch-toggled %])}]]
     [react/view styles/wrapper
      [list/flat-list {:data               (vals bootnodes)
                       :default-separator? false
                       :key-fn             :id
                       :render-fn          render-row}]]]))

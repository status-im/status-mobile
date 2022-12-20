(ns status-im.ui.screens.bootnodes-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.bootnodes-settings.styles :as styles]
            [status-im.ui.screens.profile.components.views :as profile.components]))

(defn navigate-to-add-bootnode
  [id]
  (re-frame/dispatch [:bootnodes.ui/add-bootnode-pressed id]))

(defn render-row
  [{:keys [name id]}]
  [react/touchable-highlight
   {:on-press            #(navigate-to-add-bootnode id)
    :accessibility-label :bootnode-item}
   [react/view styles/bootnode-item
    [react/view styles/bootnode-item-inner
     [react/text {:style styles/bootnode-item-name-text}
      name]]]])

(views/defview bootnodes-settings
  []
  (views/letsubs [bootnodes-enabled [:custom-bootnodes/enabled?]
                  bootnodes         [:custom-bootnodes/network-bootnodes]]
    [:<>
     [topbar/topbar
      {:title             (i18n/label :t/bootnodes-settings)
       :right-accessories
       [{:icon                :main-icons/add
         :accessibility-label :add-bootnode
         :on-press            #(navigate-to-add-bootnode nil)}]}]
     [react/view styles/switch-container
      [profile.components/settings-switch-item
       {:label-kw  :t/bootnodes-enabled
        :value     bootnodes-enabled
        :action-fn #(re-frame/dispatch [:bootnodes.ui/custom-bootnodes-switch-toggled %])}]]
     [react/view styles/wrapper
      [list/flat-list
       {:data               (vals bootnodes)
        :default-separator? false
        :key-fn             :id
        :render-fn          render-row}]]]))

(ns legacy.status-im.ui.screens.bootnodes-settings.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.bootnodes-settings.styles :as styles]
    [legacy.status-im.ui.screens.profile.components.views :as profile.components]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

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
      {:title (i18n/label :t/bootnodes-settings)
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

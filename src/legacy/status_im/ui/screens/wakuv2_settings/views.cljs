(ns legacy.status-im.ui.screens.wakuv2-settings.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.wakuv2-settings.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn navigate-to-add-node
  [id]
  (re-frame/dispatch [:wakuv2.ui/add-node-pressed id]))

(defn render-row
  [[id {:keys [name]}]]
  [react/touchable-highlight
   {:on-press            #(navigate-to-add-node id)
    :accessibility-label :wakuv2-node-item}
   [react/view styles/node-item
    [react/view styles/node-item-inner
     [react/text {:style styles/node-item-name-text}
      name]]]])

(views/defview wakuv2-settings
  []
  (views/letsubs [nodes [:wakuv2-nodes/list]]
    [:<>
     [topbar/topbar
      {:title (i18n/label :t/wakuv2-settings)
       :navigation :none
       :right-accessories
       [{:icon                :main-icons/add
         :accessibility-label :add-wakuv2-node
         :on-press            #(navigate-to-add-node nil)}]}]
     [react/view styles/wrapper
      [list/flat-list
       {:data               nodes
        :default-separator? false
        :key-fn             :id
        :render-fn          render-row}]]
     [toolbar/toolbar
      {:left
       [quo/button
        {:type     :secondary
         :after    :main-icon/close
         :on-press #(re-frame/dispatch [:wakuv2.ui/discard-all-pressed])}
        (i18n/label :t/cancel)]
       :right
       [quo/button
        {:type     :secondary
         :after    :main-icon/next
         :on-press #(re-frame/dispatch [:wakuv2.ui/save-all-pressed])}
        (i18n/label :t/save)]}]]))

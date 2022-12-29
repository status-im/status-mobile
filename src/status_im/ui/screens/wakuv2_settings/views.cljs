(ns status-im.ui.screens.wakuv2-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.wakuv2-settings.styles :as styles]))

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
      {:title             (i18n/label :t/wakuv2-settings)
       :navigation        :none
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

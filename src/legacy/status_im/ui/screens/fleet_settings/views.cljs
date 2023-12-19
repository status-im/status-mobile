(ns legacy.status-im.ui.screens.fleet-settings.views
  (:require
    [legacy.status-im.node.core :as node]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.fleet-settings.styles :as styles]
    [re-frame.core :as re-frame])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn- fleet-icon
  [current?]
  [react/view (styles/fleet-icon-container current?)
   [icons/icon :main-icons/mailserver
    (styles/fleet-icon current?)]])

(defn change-fleet
  [fleet]
  (re-frame/dispatch [:fleet.ui/fleet-selected fleet]))

(defn render-row
  [fleet _ _ current-fleet]
  (let [current? (= fleet current-fleet)]
    [react/touchable-highlight
     {:on-press            #(change-fleet fleet)
      :accessibility-label :fleet-item}
     [react/view styles/fleet-item
      [fleet-icon current?]
      [react/view styles/fleet-item-inner
       [react/text {:style styles/fleet-item-name-text}
        fleet]]]]))

(defn fleets
  [custom-fleets]
  (map name (keys (node/fleets {:custom-fleets custom-fleets}))))

(views/defview fleet-settings
  []
  (views/letsubs [custom-fleets [:fleets/custom-fleets]
                  current-fleet [:fleets/current-fleet]]
    [list/flat-list
     {:data               (fleets custom-fleets)
      :default-separator? false
      :key-fn             identity
      :render-data        (name current-fleet)
      :render-fn          render-row}]))

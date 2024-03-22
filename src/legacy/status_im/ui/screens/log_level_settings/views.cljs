(ns legacy.status-im.ui.screens.log-level-settings.views
  (:require
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.log-level-settings.styles :as styles]
    [re-frame.core :as re-frame])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn- log-level-icon
  [current?]
  [react/view (styles/log-level-icon-container current?)
   [icons/icon :main-icons/mailserver
    (styles/log-level-icon current?)]])

(defn change-log-level
  [log-level]
  (re-frame/dispatch [:log-level.ui/log-level-selected log-level]))

(defn render-row
  [{:keys [name value] :as log-level} _ _ current-log-level]
  (let [current? (= value current-log-level)]
    [react/touchable-highlight
     {:on-press            #(change-log-level log-level)
      :accessibility-label :log-level-item}
     [react/view styles/log-level-item
      [log-level-icon current?]
      [react/view styles/log-level-item-inner
       [react/text {:style styles/log-level-item-name-text}
        name]]]]))

(def log-levels
  [{:name  "DISABLED"
    :value ""}
   {:name  "ERROR"
    :value "ERROR"}
   {:name  "WARN"
    :value "WARN"}
   {:name  "INFO"
    :value "INFO"}
   {:name  "DEBUG"
    :value "DEBUG"}
   {:name  "TRACE"
    :value "TRACE"}])

(views/defview log-level-settings
  []
  (views/letsubs [current-log-level [:log-level/current-log-level]]
    [list/flat-list
     {:data               log-levels
      :default-separator? false
      :key-fn             :name
      :render-data        current-log-level
      :render-fn          render-row}]))

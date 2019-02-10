(ns status-im.ui.screens.log-level-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.log-level-settings.styles :as styles]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn- log-level-icon [current?]
  [react/view (if platform/desktop?
                {:style (styles/log-level-icon-container current?)}
                (styles/log-level-icon-container current?))
   [vector-icons/icon :main-icons/log-level
    (if platform/desktop? {:style (styles/log-level-icon current?)}
        (styles/log-level-icon current?))]])

(defn change-log-level [log-level]
  (re-frame/dispatch [:log-level.ui/log-level-selected log-level]))

(defn render-row [current-log-level]
  (fn [{:keys [name value] :as log-level}]
    (let [current? (= value current-log-level)]
      [react/touchable-highlight
       {:on-press #(change-log-level log-level)
        :accessibility-label :log-level-item}
       [react/view styles/log-level-item
        [log-level-icon current?]
        [react/view styles/log-level-item-inner
         [react/text {:style styles/log-level-item-name-text}
          name]]]])))

(def log-levels
  [{:name "DISABLED"
    :value ""}
   {:name "ERROR"
    :value "ERROR"}
   {:name "WARN"
    :value "WARN"}
   {:name "INFO"
    :value "INFO"}
   {:name "DEBUG"
    :value "DEBUG"}
   {:name "TRACE"
    :value "TRACE"}])

(views/defview log-level-settings []
  (views/letsubs [current-log-level [:settings/current-log-level]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/log-level-settings)]]
     [react/view styles/wrapper
      [list/flat-list {:data               log-levels
                       :default-separator? false
                       :key-fn             :name
                       :render-fn          (render-row current-log-level)}]]]))

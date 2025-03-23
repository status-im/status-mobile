(ns legacy.status-im.ui.screens.log-level-settings.views
  (:require
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.log-level-settings.styles :as styles]
    [quo.core :as quo]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn- log-level-icon
  [current?]
  [react/view (styles/log-level-icon-container current?)
   [icons/icon :main-icons/mailserver
    (styles/log-level-icon current?)]])

(defn change-log-level
  [{:keys [value]}]
  (re-frame/dispatch [:log-level.ui/change-log-level-confirmed value]))

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
  "The status-go log library (zap) doesn't support the trace level, so we remove
  the trace option from the UI. When trace is enabled an error will happen while
  trying to login (user will see the error 'wrong password')."
  [{:name  "DISABLED"
    :value ""}
   {:name  "ERROR"
    :value "ERROR"}
   {:name  "WARN"
    :value "WARN"}
   {:name  "INFO"
    :value "INFO"}
   {:name  "DEBUG"
    :value "DEBUG"}])

(views/defview log-level-settings
  []
  (views/letsubs [current-log-level [:log-level/current-log-level]]
    [:<>
     [quo/page-nav
      {:type       :title
       :title      (i18n/label :t/log-level-settings)
       :background :blur
       :icon-name  :i/close
       :on-press   #(rf/dispatch [:navigate-back])}]
     [list/flat-list
      {:data               log-levels
       :default-separator? false
       :key-fn             :name
       :render-data        current-log-level
       :render-fn          render-row}]]))

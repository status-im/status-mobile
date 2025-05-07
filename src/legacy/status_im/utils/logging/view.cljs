(ns legacy.status-im.utils.logging.view
  (:require
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.log-level-settings.styles :as styles]
    [quo.context]
    [quo.core :as quo]
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
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
  (re-frame/dispatch [:log-level.ui/change-pre-login-log-level value]))

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

(views/defview pre-login-log-level-settings
  []
  (views/letsubs [current-log-level [:log-level/pre-login-log-level]]
    [:<>
     [list/flat-list
      {:data               log-levels
       :default-separator? false
       :key-fn             :name
       :render-data        current-log-level
       :render-fn          render-row}]]))

(views/defview logs-management-drawer
  []
  (views/letsubs [logged-in? [:multiaccount/logged-in?]]
    [quo/action-drawer
     [[{:label     (i18n/label :t/send-logs)
        :sub-label (i18n/label :t/send-logs-to
                               {:email constants/report-email})
        :on-press  #(rf/dispatch [:open-modal :bug-report])}
       {:label    (i18n/label :t/share-logs)
        :on-press #(rf/dispatch [:logging.ui/send-logs-pressed :sharing true])}
       (when-not logged-in?
         {:label    (i18n/label :t/set-pre-login-log-level)
          :on-press #(rf/dispatch [:show-bottom-sheet {:content pre-login-log-level-settings}])})]]]))

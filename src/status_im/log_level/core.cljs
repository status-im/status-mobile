(ns status-im.log-level.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.node.core :as node]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]))

(fx/defn save-log-level
  [{:keys [db now] :as cofx} log-level]
  (let [old-log-level (get-in db [:multiaccount :log-level])]
    (when (not= old-log-level log-level)
      (fx/merge cofx
                (multiaccounts.update/multiaccount-update
                 :log-level log-level
                 {})
                (node/prepare-new-config
                 {:on-success #(re-frame/dispatch [:logout])})))))

(fx/defn show-change-log-level-confirmation
  [{:keys [db]} {:keys [name value] :as log-level}]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/change-log-level
                                     {:log-level name})
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept           #(re-frame/dispatch
                           [:log-level.ui/change-log-level-confirmed value])
    :on-cancel           nil}})

(fx/defn show-logging-enabled-confirmation
  [{:keys [db]} enabled]
  {:ui/show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/change-logging-enabled
                                                           {:enable (i18n/label (if enabled
                                                                                  :enable :disable))})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [:log-level.ui/logging-enabled-confirmed enabled])
                          :on-cancel           nil}})

;;FIXME ignored until desktop is fixed
#_(fx/defn save-logging-enabled
    [{:keys [db] :as cofx}  enabled]
    (.setValue rn-dependencies/desktop-config "logging_enabled" enabled)
    (fx/merge
     cofx
     {:db (assoc-in db [:desktop/desktop :logging-enabled] enabled)}
     (multiaccounts.update/multiaccount-update
      {:log-level (when enabled "INFO")}
      {:success-event [:multiaccounts.update.callback/save-settings-success]})))

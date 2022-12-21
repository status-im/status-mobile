(ns status-im.log-level.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.node.core :as node]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.fx :as fx]))

(fx/defn save-log-level
  {:events [:log-level.ui/change-log-level-confirmed]}
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
  {:events [:log-level.ui/log-level-selected]}
  [_ {:keys [name value]}]
  {:ui/show-confirmation
   {:title               (i18n/label :t/close-app-title)
    :content             (i18n/label :t/change-log-level
                                     {:log-level name})
    :confirm-button-text (i18n/label :t/close-app-button)
    :on-accept           #(re-frame/dispatch
                           [:log-level.ui/change-log-level-confirmed value])
    :on-cancel           nil}})

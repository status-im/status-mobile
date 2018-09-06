(ns status-im.log-level.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn save-log-level
  [log-level {:keys [db now] :as cofx}]
  (let [settings (get-in db [:account/account :settings])]
    (handlers-macro/merge-fx cofx
                             (accounts.update/update-settings
                              (if log-level
                                (assoc settings :log-level log-level)
                                (dissoc settings :log-level))
                              [:accounts.update.callback/save-settings-success]))))

(defn show-change-log-level-confirmation
  [{:keys [name value] :as log-level} {:keys [db]}]
  {:ui/show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/change-log-level
                                                           {:log-level name})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(re-frame/dispatch [:log-level.ui/change-log-level-confirmed value])
                          :on-cancel           nil}})

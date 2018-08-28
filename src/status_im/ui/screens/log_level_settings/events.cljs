(ns status-im.ui.screens.log-level-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(handlers/register-handler-fx
 ::save-log-level
 (fn [{:keys [db now] :as cofx} [_ log-level]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              (accounts.models/update-settings
                               (if log-level
                                 (assoc settings :log-level log-level)
                                 (dissoc settings :log-level))
                               [:logout])))))

(handlers/register-handler-fx
 :change-log-level
 (fn [{:keys [db]} [_ log-level]]
   {:show-confirmation {:title               (i18n/label :t/close-app-title)
                        :content             (i18n/label :t/change-log-level
                                                         {:log-level log-level})
                        :confirm-button-text (i18n/label :t/close-app-button)
                        :on-accept           #(re-frame/dispatch [::save-log-level log-level])
                        :on-cancel           nil}}))

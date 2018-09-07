(ns status-im.ui.screens.fleet-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

(handlers/register-handler-fx
 ::save-fleet
 (fn [{:keys [db now] :as cofx} [_ fleet]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              (accounts.models/update-settings
                               (if fleet
                                 (assoc settings :fleet fleet)
                                 (dissoc settings :fleet))
                               [:logout])))))

(handlers/register-handler-fx
 :change-fleet
 (fn [{:keys [db]} [_ fleet]]
   {:show-confirmation {:title               (i18n/label :t/close-app-title)
                        :content             (i18n/label :t/change-fleet
                                                         {:fleet fleet})
                        :confirm-button-text (i18n/label :t/close-app-button)
                        :on-accept           #(re-frame/dispatch [::save-fleet (keyword fleet)])
                        :on-cancel           nil}}))

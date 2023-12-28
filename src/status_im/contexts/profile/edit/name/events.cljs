(ns status-im.contexts.profile.edit.name.events
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as re-frame]))

(re-frame/reg-event-fx :profile/edit-name
 (fn [{:keys [db]} [name]]
   {:db            (update db :profile/profile merge {:display-name name})
    :json-rpc/call [{:method     "wakuext_setDisplayName"
                     :params     [name]
                     :on-success (fn []
                                   (re-frame/dispatch [:navigate-back])
                                   (re-frame/dispatch [:toasts/upsert
                                                       {:type :positive
                                                        :text (i18n/label :t/name-updated)}]))}]}))

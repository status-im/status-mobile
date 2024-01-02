(ns status-im.contexts.profile.edit.name.events
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as re-frame]))

(defn edit-profile-name
  [{:keys [db]} [name]]
  {:db            (assoc-in db [:profile/profile :display-name] name)
   :json-rpc/call [{:method     "wakuext_setDisplayName"
                    :params     [name]
                    :on-success (fn []
                                  (re-frame/dispatch [:navigate-back])
                                  (re-frame/dispatch [:toasts/upsert
                                                      {:type :positive
                                                       :text (i18n/label :t/name-updated)}]))}]})

(re-frame/reg-event-fx :profile/edit-name edit-profile-name)

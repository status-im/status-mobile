(ns status-im.contexts.profile.edit.name.events
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn edit-profile-name
  [{:keys [db]} [name]]
  {:db (assoc-in db [:profile/profile :display-name] name)
   :fx [[:json-rpc/call
         [{:method     "wakuext_setDisplayName"
           :params     [name]
           :on-success (fn []
                         (rf/dispatch [:navigate-back])
                         (rf/dispatch [:toasts/upsert
                                       {:type  :positive
                                        :theme :dark
                                        :text  (i18n/label :t/name-updated)}]))}]]]})

(rf/reg-event-fx :profile/edit-name edit-profile-name)

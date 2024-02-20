(ns status-im.contexts.profile.edit.bio.events
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-profile-bio-success
 (fn [_]
   {:fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/bio-added)}]]]}))

(defn edit-profile-bio
  [{:keys [db]} [bio]]
  {:db (assoc-in db [:profile/profile :bio] bio)
   :fx [[:json-rpc/call
         [{:method     "wakuext_setBio"
           :params     [bio]
           :on-success [:profile/edit-profile-bio-success]}]]]})

(rf/reg-event-fx :profile/edit-bio edit-profile-bio)

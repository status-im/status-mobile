(ns status-im.contexts.profile.edit.bio.events
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-profile-bio-success
 (fn [_ [added?]]
   {:fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (if added?
                     (i18n/label :t/bio-added)
                     (i18n/label :t/bio-updated))}]]]}))

(defn edit-profile-bio
  [{:keys [db]} [new-bio]]
  (let [{:keys [bio]} (:profile/profile db)]
    {:db (assoc-in db [:profile/profile :bio] new-bio)
     :fx [[:json-rpc/call
           [{:method     "wakuext_setBio"
             :params     [new-bio]
             :on-success [:profile/edit-profile-bio-success (string/blank? bio)]}]]]}))

(rf/reg-event-fx :profile/edit-bio edit-profile-bio)

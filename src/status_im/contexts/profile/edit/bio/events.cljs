(ns status-im.contexts.profile.edit.bio.events
  (:require [clojure.string :as string]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-profile-bio-success
 (fn [_ [{:keys [bio added?]}]]
   {:fx [[:dispatch [:profile/save-local-profile-bio bio]]
         [:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (if added?
                     (i18n/label :t/bio-added)
                     (i18n/label :t/bio-updated))}]]]}))

(rf/reg-event-fx :profile/save-local-profile-bio
 (fn [{:keys [db]} [bio]]
   {:db (assoc-in db [:profile/profile :bio] bio)}))

(defn edit-profile-bio
  [{:keys [db]} [new-bio]]
  (let [{:keys [bio]} (:profile/profile db)]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_setBio"
             :params     [new-bio]
             :on-success [:profile/edit-profile-bio-success
                          {:bio    new-bio
                           :added? (string/blank? bio)}]
             :on-error   #(log/error "failed to set bio " %)}]]]}))

(rf/reg-event-fx :profile/edit-bio edit-profile-bio)

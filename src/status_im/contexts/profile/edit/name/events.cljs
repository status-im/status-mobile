(ns status-im.contexts.profile.edit.name.events
  (:require [utils.i18n :as i18n]
            [status-im.contexts.profile.utils :as utils]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-profile-name-success
 (fn [{:keys [db]} [display-name navigate-back? show-toast?]]
   {:db (assoc-in db [:profile/profile :display-name] display-name)
    :fx [(when navigate-back?
           [:dispatch [:navigate-back]])
         (when show-toast?
           [:dispatch
            [:toasts/upsert
             {:type  :positive
              :theme :dark
              :text  (i18n/label :t/name-updated)}]])]}))

(defn edit-profile-name
  [_
   [{:keys [display-name navigate-back? show-toast?]
     :or   {navigate-back? true
            show-toast?    true}}]]
  {:fx [[:json-rpc/call
         [{:method     "wakuext_setDisplayName"
           :params     [display-name]
           :on-success [:profile/edit-profile-name-success display-name navigate-back? show-toast?]}]]]})

(rf/reg-event-fx :profile/edit-name edit-profile-name)

(rf/reg-event-fx
 :profile/set-default-profile-name
 (fn [{db :db} [profile]]
   (let [default-display-name (utils/display-name-from-compressed-key profile)]
     {:db (assoc-in db [:profile/profile :display-name] default-display-name)
      :fx [[:json-rpc/call
            [{:method "wakuext_setDisplayName"
              :params [default-display-name]}]]]})))

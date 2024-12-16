(ns status-im.contexts.profile.edit.header.events
  (:require [clojure.string :as string]
            [status-im.common.avatar-picture-picker.view :as profile-picture-picker]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/update-local-picture
 (fn [{:keys [db now]} [images]]
   {:db (if images
          (assoc-in db
           [:profile/profile :images]
           (map #(assoc % :clock now) images))
          (update db :profile/profile dissoc :images))}))

(rf/reg-event-fx :profile/edit-profile-picture-success
 (fn [_ [images]]
   (let [has-picture? (rf/sub [:profile/has-picture])]
     {:fx [[:dispatch [:profile/update-local-picture (reverse images)]]
           [:dispatch
            [:toasts/upsert
             {:type  :positive
              :theme :dark
              :text  (i18n/label (if has-picture?
                                   :t/profile-picture-updated
                                   :t/profile-picture-added))}]]]})))

(defn edit-profile-picture
  [{:keys [db]} [picture crop-width crop-height]]
  (let [key-uid     (get-in db [:profile/profile :key-uid])
        crop-width  (or crop-width profile-picture-picker/crop-size)
        crop-height (or crop-height profile-picture-picker/crop-size)
        path        (string/replace-first picture #"file://" "")]
    {:fx [[:json-rpc/call
           [{:method     "multiaccounts_storeIdentityImage"
             :params     [key-uid path 0 0 crop-width crop-height]
             :on-success [:profile/edit-profile-picture-success]}]]]}))

(rf/reg-event-fx :profile/edit-picture edit-profile-picture)

(rf/reg-event-fx :profile/delete-profile-picture-success
 (fn [_]
   {:fx [[:dispatch [:profile/update-local-picture nil]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/profile-picture-removed)}]]]}))

(defn delete-profile-picture
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:json-rpc/call
           [{:method     "multiaccounts_deleteIdentityImage"
             :params     [key-uid]
             :on-success [:profile/delete-profile-picture-success]}]]]}))

(rf/reg-event-fx :profile/delete-picture delete-profile-picture)

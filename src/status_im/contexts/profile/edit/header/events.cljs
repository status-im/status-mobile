(ns status-im.contexts.profile.edit.header.events
  (:require [status-im.common.profile-picture-picker.view :as photo-picker]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/update-local-picture
 (fn [{:keys [db]} [images]]
   {:db (if images
          (assoc-in db [:profile/profile :images] images)
          (update db :profile/profile dissoc :images))}))

(defn edit-profile-picture
  [{:keys [db]} [picture crop-width crop-height]]
  (let [key-uid     (get-in db [:profile/profile :key-uid])
        crop-width  (or crop-width photo-picker/crop-size)
        crop-height (or crop-height photo-picker/crop-size)]
    {:json-rpc/call
     [{:method     "multiaccounts_storeIdentityImage"
       :params     [key-uid picture 0 0 crop-width crop-height]
       :on-success (fn [images]
                     (rf/dispatch [:profile/update-local-picture (reverse images)])
                     (rf/dispatch [:toasts/upsert
                                   {:type  :positive
                                    :theme :dark
                                    :text  (i18n/label :t/profile-picture-added)}]))}]}))

(defn delete-profile-picture
  [{:keys [db]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:json-rpc/call
     [{:method     "multiaccounts_deleteIdentityImage"
       :params     [key-uid]
       :on-success (fn []
                     (rf/dispatch [:profile/update-local-picture nil])
                     (rf/dispatch [:toasts/upsert
                                   {:type  :positive
                                    :theme :dark
                                    :text  (i18n/label :t/profile-picture-removed)}]))}]}))

(rf/reg-event-fx :profile/edit-picture edit-profile-picture)

(rf/reg-event-fx :profile/delete-picture delete-profile-picture)

(ns status-im.contexts.profile.edit.modal.events
  (:require [status-im.contexts.profile.edit.introduce-yourself.view :as introduce-yourself]
            [utils.re-frame :as rf]))

(rf/reg-event-fx
 :profile/edit-profile
 (fn [_ [{:keys [display-name picture color on-success]}]]
   {:fx (cond-> []
          display-name
          (conj [:dispatch
                 [:profile/edit-name
                  {:display-name   display-name
                   :navigate-back? false
                   :show-toast?    false}]])
          color
          (conj [:dispatch
                 [:profile/edit-accent-colour
                  {:color          color
                   :navigate-back? false
                   :show-toast?    false}]])
          picture
          (conj [:dispatch
                 [:profile/edit-picture
                  {:picture     picture
                   :show-toast? false}]])

          (nil? picture)
          (conj [:dispatch [:profile/delete-picture {:show-toast? false}]])

          :always
          (conj [:dispatch [:navigate-back]]
                [:dispatch on-success]))}))

(defn- profile-update-asked-storage-key
  [key-uid]
  (keyword :update-profile-asked key-uid))

(rf/reg-event-fx
 :profile/set-profile-update-as-asked
 (fn [{db :db}]
   (let [storage-key (-> db :profile/profile :key-uid profile-update-asked-storage-key)]
     {:fx [[:effects.async-storage/set {storage-key true}]]})))

(rf/reg-event-fx
 :profile/ask-profile-update
 (fn [_ [pending-event]]
   {:fx [[:dispatch [:profile/set-profile-update-as-asked]]
         [:dispatch
          [:show-bottom-sheet
           {:content (fn []
                       [introduce-yourself/sheet {:pending-event pending-event}])}]]]}))

(rf/reg-event-fx
 :profile/check-profile-update-prompt
 (fn [{db :db} [pending-event]]
   (let [storage-key (-> db :profile/profile :key-uid profile-update-asked-storage-key)]
     {:fx [[:effects.async-storage/get
            {:keys [storage-key]
             :cb   (fn [profile-updated-data]
                     (rf/dispatch (if (storage-key profile-updated-data)
                                    pending-event
                                    [:profile/ask-profile-update pending-event])))}]]})))

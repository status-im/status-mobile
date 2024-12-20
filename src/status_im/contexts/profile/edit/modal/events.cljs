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
          (conj [:dispatch-n [[:navigate-back] on-success]]))}))


(rf/reg-event-fx
 :profile/ask-profile-update
 (fn [_ [pending-event]]
   {:fx [[:effects.async-storage/set {:update-profile-asked? true}]
         [:dispatch
          [:show-bottom-sheet
           {:content (fn []
                       [introduce-yourself/sheet {:pending-event pending-event}])}]]]}))

(rf/reg-event-fx
 :profile/check-profile-update-prompt
 (fn [_ [pending-event]]
   {:fx [[:effects.async-storage/get
          {:keys [:update-profile-asked?]
           :cb   (fn [{:keys [update-profile-asked?]}]
                   (rf/dispatch (if update-profile-asked?
                                  pending-event
                                  [:profile/ask-profile-update pending-event])))}]]}))

(ns status-im.contexts.profile.edit.accent-colour.events
  (:require [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-accent-colour-success
 (fn [_]
   {:fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/accent-colour-updated)}]]]}))

(defn edit-accent-colour
  [{:keys [db]} [customization-color]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:db (assoc-in db [:profile/profile :customization-color] customization-color)
     :fx [[:json-rpc/call
           [{:method     "wakuext_setCustomizationColor"
             :params     [{:customizationColor customization-color
                           :keyUid             key-uid}]
             :on-success [:profile/edit-accent-colour-success]
             :on-error   #(log/error "failed to edit accent color." {:error %})}]]]}))

(rf/reg-event-fx :profile/edit-accent-colour edit-accent-colour)

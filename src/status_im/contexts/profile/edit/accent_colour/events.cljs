(ns status-im.contexts.profile.edit.accent-colour.events
  (:require [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-accent-colour-success
 (fn [_ [customization-color]]
   {:fx [[:dispatch [:profile/save-local-accent-color customization-color]]
         [:dispatch [:navigate-back]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/accent-colour-updated)}]]]}))

(rf/reg-event-fx :profile/save-local-accent-color
 (fn [{:keys [db]} [customization-color]]
   {:db (assoc-in db [:profile/profile :customization-color] customization-color)}))

(defn edit-accent-colour
  [{:keys [db]} [customization-color]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_setCustomizationColor"
             :params     [{:customizationColor customization-color
                           :keyUid             key-uid}]
             :on-success [:profile/edit-accent-colour-success customization-color]
             :on-error   #(log/error "failed to edit accent color." {:error %})}]]]}))

(rf/reg-event-fx :profile/edit-accent-colour edit-accent-colour)

(ns status-im.contexts.profile.edit.accent-colour.events
  (:require [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :profile/edit-accent-colour-success
 (fn [_ [{:keys [customization-color navigate-back? show-toast?]}]]
   {:fx [[:dispatch [:profile/save-local-accent-color customization-color]]
         (when navigate-back?
           [:dispatch [:navigate-back]])
         (when show-toast?
           [:dispatch
            [:toasts/upsert
             {:type  :positive
              :theme :dark
              :text  (i18n/label :t/accent-colour-updated)}]])]}))

(rf/reg-event-fx :profile/save-local-accent-color
 (fn [{:keys [db]} [customization-color]]
   {:db (assoc-in db [:profile/profile :customization-color] customization-color)}))

(defn edit-accent-colour
  [{:keys [db]}
   [{:keys [color navigate-back? show-toast?]
     :or   {navigate-back? true
            show-toast?    true}}]]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_setCustomizationColor"
             :params     [{:customizationColor color
                           :keyUid             key-uid}]
             :on-success [:profile/edit-accent-colour-success
                          {:customization-color color
                           :navigate-back?      navigate-back?
                           :show-toast?         show-toast?}]
             :on-error   #(log/error "failed to edit accent color." {:error %})}]]]}))

(rf/reg-event-fx :profile/edit-accent-colour edit-accent-colour)

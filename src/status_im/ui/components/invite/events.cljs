(ns status-im.ui.components.invite.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.react :as react]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(rf/reg-event-fx
 :invite.events/share-link
 (fn [_]
   {:dispatch [:universal-links/generate-profile-url
               {:cb #(rf/dispatch [:invite.events/share-link-after-profile-url-generated])}]}))

(rf/reg-event-fx
 :invite.events/share-link-after-profile-url-generated
 (fn [{:keys [db]}]
   (let [{:keys [universal-profile-url]} (get db :profile/profile)
         message                         (i18n/label :t/join-me {:url universal-profile-url})]
     {::share {:message message}})))

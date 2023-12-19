(ns legacy.status-im.ui.components.invite.events
  (:require
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(rf/reg-event-fx
 :invite.events/share-link
 (fn [{:keys [db]}]
   (let [{:keys [universal-profile-url]} (get db :profile/profile)
         message                         (i18n/label :t/join-me {:url universal-profile-url})]
     {::share {:message message}})))

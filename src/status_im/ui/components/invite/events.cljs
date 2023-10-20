(ns status-im.ui.components.invite.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.react :as react]
    [status-im.utils.universal-links.utils :as universal-links]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(rf/defn share-link
  {:events [:invite.events/share-link]}
  [{:keys [db]}]
  (let [{:keys [public-key preferred-name]} (get db :profile/profile)
        profile-link                        (universal-links/generate-link :user
                                                                           :external
                                                                           (or preferred-name
                                                                               public-key))
        message                             (i18n/label :t/join-me {:url profile-link})]
    {::share {:message message}}))

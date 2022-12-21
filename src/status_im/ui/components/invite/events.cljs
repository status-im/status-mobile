(ns status-im.ui.components.invite.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.universal-links.utils :as universal-links]))

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(fx/defn share-link
  {:events [::share-link]}
  [{:keys [db]}]
  (let [{:keys [public-key preferred-name]} (get db :multiaccount)
        profile-link                        (universal-links/generate-link :user :external
                                                                           (or preferred-name public-key))
        message                             (i18n/label :t/join-me {:url profile-link})]
    {::share {:message message}}))

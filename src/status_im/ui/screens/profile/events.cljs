(ns status-im.ui.screens.profile.events
  (:require [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.ui.screens.profile.models :as profile.models]
            [status-im.ui.screens.profile.navigation]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.fx :as fx]))

(re-frame/reg-fx
 :open-image-picker
 ;; the image picker is only used here for now, this effect can be use in other scenarios as well
 (fn [callback-event]
   (profile.models/open-image-picker! callback-event)))

(handlers/register-handler-fx
 :profile/send-transaction
 (fn [cofx [_ chat-id]]
   (profile.models/send-transaction chat-id cofx)))

(handlers/register-handler-fx
 :my-profile/update-name
 (fn [cofx [_ name]]
   (profile.models/update-name name cofx)))

(handlers/register-handler-fx
 :my-profile/update-picture
 (fn [cofx [this-event base64-image]]
   (profile.models/update-picture this-event base64-image cofx)))

(handlers/register-handler-fx
 :my-profile/remove-current-photo
 (fn [{:keys [db] :as cofx}]
   (fx/merge cofx
             {:db (-> db
                      (assoc-in [:my-profile/profile :photo-path]
                                (identicon/identicon (multiaccounts.model/current-public-key cofx)))
                      (assoc :my-profile/editing? true
                             :profile/photo-added? false))}
             (profile.models/save))))

(handlers/register-handler-fx
 :my-profile/start-editing-profile
 (fn [cofx _]
   (profile.models/start-editing cofx)))

(handlers/register-handler-fx
 :my-profile/save-profile
 (fn [cofx _]
   (profile.models/save cofx)))

(handlers/register-handler-fx
 :group-chat-profile/start-editing
 (fn [cofx _]
   (profile.models/start-editing-group-chat-profile cofx)))

(handlers/register-handler-fx
 :my-profile/enter-two-random-words
 (fn [cofx _]
   (profile.models/enter-two-random-words cofx)))

(handlers/register-handler-fx
 :my-profile/set-step
 (fn [cofx [_ step]]
   (profile.models/set-step step cofx)))

(handlers/register-handler-fx
 :my-profile/finish
 (fn [cofx _]
   (profile.models/finish cofx)))

(re-frame/reg-fx
 :copy-to-clipboard
 (fn [value]
   (profile.models/copy-to-clipboard! value)))

(handlers/register-handler-fx
 :copy-to-clipboard
 (fn [_ [_ value]]
   {:copy-to-clipboard value}))

(re-frame/reg-fx
 :show-tooltip
 profile.models/show-tooltip!)

(handlers/register-handler-fx
 :show-tooltip
 (fn [_ [_ tooltip-id]]
   {:show-tooltip tooltip-id}))

(re-frame/reg-fx
 :profile/share-profile-link
 (fn [contact-code]
   (let [link (universal-links/generate-link :user :external contact-code)]
     (list-selection/open-share {:message link}))))

(handlers/register-handler-fx
 :profile/share-profile-link
 (fn [_ [_ value]]
   {:profile/share-profile-link value}))

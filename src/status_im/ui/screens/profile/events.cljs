(ns status-im.ui.screens.profile.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.profile.models :as profile.models]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.universal-links.utils :as universal-links]))

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

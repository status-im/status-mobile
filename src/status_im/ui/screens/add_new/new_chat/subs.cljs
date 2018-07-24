(ns status-im.ui.screens.add-new.new-chat.subs
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.add-new.new-chat.db :as db]))

(re-frame/reg-sub
 :new-contact-error-message
 :<- [:get :contacts/new-identity]
 :<- [:get-current-account]
 (fn [[new-identity account]]
   (db/validate-pub-key new-identity account)))

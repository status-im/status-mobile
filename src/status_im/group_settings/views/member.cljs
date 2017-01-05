(ns status-im.group-settings.views.member
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.contacts.views.contact :refer [contact-view]]
            [status-im.i18n :refer [label]]))

(defn member-view [{:keys [whisper-identity role] :as contact}]
  ;; TODO implement :role property for group chat contact
  [contact-view
   {:contact   contact
    :extended? true
    :info      role
    :on-click  #(dispatch [:set :selected-participants #{whisper-identity}])}])

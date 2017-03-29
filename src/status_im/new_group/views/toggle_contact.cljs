(ns status-im.new-group.views.toggle-contact
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.contact.contact :refer [toogle-contact-view]]))

(defn on-toggle [checked? whisper-identity]
  (let [action (if checked? :deselect-contact :select-contact)]
    (dispatch [action whisper-identity])))

(defn on-toggle-participant [checked? whisper-identity]
  (let [action (if checked? :deselect-participant :select-participant)]
    (dispatch [action whisper-identity])))

(defn group-toggle-contact [{:keys [whisper-identity] :as contact}]
  [toogle-contact-view contact :is-contact-selected? on-toggle])

(defn group-toggle-participant [{:keys [whisper-identity] :as contact}]
  [toogle-contact-view contact :is-participant-selected? on-toggle-participant])
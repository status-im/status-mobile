(ns status-im.data-store.contacts
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.contacts :as data-store])
  (:refer-clojure :exclude [exists?]))

(re-frame/reg-cofx
 :data-store/get-all-contacts
 (fn [coeffects _]
   (assoc coeffects :all-contacts (data-store/get-all-as-list))))

(defn- get-by-id
  [whisper-identity]
  (data-store/get-by-id-cljs whisper-identity))

(defn- save
  [{:keys [whisper-identity pending?] :as contact}]
  (let [{pending-db? :pending?
         :as         contact-db} (get-by-id whisper-identity)
        contact' (-> contact
                     (assoc :pending? (boolean (if contact-db
                                                 (if (nil? pending?) pending-db? pending?)
                                                 pending?)))
                     (dissoc :command :response :subscriptions :jail-loaded-events))]
    (data-store/save contact' (boolean contact-db))))

(re-frame/reg-fx
 :data-store/save-contact
 (fn [contact]
   (async/go (async/>! core/realm-queue #(save contact)))))

(re-frame/reg-fx
 :data-store/save-contacts
 (fn [contacts]
   (doseq [contact contacts]
     (async/go (async/>! core/realm-queue #(save contact))))))

(re-frame/reg-fx
 :data-store/delete-contact
 (fn [contact]
   (async/go (async/>! core/realm-queue #(data-store/delete contact)))))

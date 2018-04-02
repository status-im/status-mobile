(ns status-im.data-store.contact-groups
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.contact-groups :as data-store])
  (:refer-clojure :exclude [exists?]))

(defn- normalize-contacts
  [item]
  (update item :contacts vals))

(re-frame/reg-cofx
  :data-store/get-all-contact-groups
  (fn [cofx _]
    (assoc cofx :all-contact-groups (into {}
                                          (map (comp (juxt :group-id identity) normalize-contacts))
                                          (data-store/get-all-as-list)))))

(re-frame/reg-fx
  :data-store/save-contact-group
  (fn [{:keys [group-id] :as group}]
    (async/go (async/>! core/realm-queue #(data-store/save group (data-store/exists? group-id))))))

(re-frame/reg-fx
  :data-store/save-contact-groups
  (fn [groups]
    (doseq [{:keys [group-id] :as group} groups]
      (async/go (async/>! core/realm-queue #(data-store/save group (data-store/exists? group-id)))))))

(re-frame/reg-fx
  :data-store/save-contact-group-property
  (fn [[group-id property-name value]]
    (async/go (async/>! core/realm-queue #(data-store/save-property group-id property-name value)))))

(re-frame/reg-fx
  :data-store/add-contacts-to-contact-group
  (fn [[group-id contacts]]
    (async/go (async/>! core/realm-queue #(data-store/add-contacts group-id contacts)))))

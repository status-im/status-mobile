(ns status-im.data-store.core
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            status-im.data-store.chats
            status-im.data-store.messages
            status-im.data-store.contacts
            status-im.data-store.transport
            status-im.data-store.browser
            status-im.data-store.accounts
            status-im.data-store.local-storage
            status-im.data-store.contact-groups
            status-im.data-store.requests))

(defn init [encryption-key]
  (when-not @core/base-realm
    (core/open-base-realm encryption-key))
  (core/reset-account-realm encryption-key))

(defn change-account [address new-account? encryption-key handler]
  (core/change-account address new-account? encryption-key handler))

(re-frame/reg-fx
  :data-store/base-tx
  (fn [transactions]
    (async/go (async/>! core/realm-queue (fn []
                                           (let [realm @core/base-realm]
                                             (core/write realm
                                                         #(doseq [transaction transactions]
                                                            (transaction realm)))))))))

(re-frame/reg-fx
  :data-store/tx
  (fn [transactions]
    (async/go (async/>! core/realm-queue (fn []
                                           (let [realm @core/account-realm]
                                             (core/write realm
                                                         #(doseq [transaction transactions]
                                                            (transaction realm)))))))))

(ns status-im.data-store.core
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as data-source]
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
  (when-not @data-source/base-realm
    (data-source/open-base-realm encryption-key))
  (data-source/reset-account-realm encryption-key))

(defn change-account [address new-account? encryption-key handler]
  (data-source/change-account address new-account? encryption-key handler))

(defn- perform-transactions [transactions realm]
  (data-source/write realm #(doseq [transaction transactions]
                              (transaction realm))))

(re-frame/reg-fx
 :data-store/base-tx
 (fn [transactions]
   (async/go (async/>! data-source/realm-queue
                       (partial perform-transactions
                                transactions
                                @data-source/base-realm)))))

(re-frame/reg-fx
 :data-store/tx
 (fn [transactions]
   (async/go (async/>! data-source/realm-queue
                       (partial perform-transactions
                                transactions
                                @data-source/account-realm)))))

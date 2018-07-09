(ns status-im.data-store.core
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.realm.core :as data-source]
            status-im.data-store.chats
            status-im.data-store.messages
            status-im.data-store.contacts
            status-im.data-store.transport
            status-im.data-store.browser
            status-im.data-store.accounts
            status-im.data-store.local-storage
            status-im.data-store.mailservers
            status-im.data-store.requests))

(defn init [encryption-key]
  (when-not @data-source/base-realm
    (data-source/open-base-realm encryption-key)))

(defn change-account [address encryption-key]
  (log/debug "changing account to: " address)
  (data-source/change-account address encryption-key))

(defn- perform-transactions [raw-transactions realm]
  (let [success-events (keep :success-event raw-transactions)
        transactions   (map (fn [{:keys [transaction] :as f}]
                              (or transaction f)) raw-transactions)]
    (data-source/write realm #(doseq [transaction transactions]
                                (transaction realm)))
    (doseq [event success-events]
      (re-frame/dispatch event))))

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

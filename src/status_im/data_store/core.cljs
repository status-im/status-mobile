(ns status-im.data-store.core
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.realm.core :as data-source]
            status-im.data-store.chats
            status-im.data-store.messages
            status-im.data-store.contacts
            status-im.data-store.installations
            status-im.data-store.transport
            status-im.data-store.browser
            status-im.data-store.multiaccounts
            status-im.data-store.mailservers))

(defn init [encryption-key]
  (if @data-source/base-realm
    (js/Promise.resolve)
    (..
     (data-source/ensure-directories)
     ;; This can be removed when we are confident all the users
     ;; have migrated the data, introduced in 0.9.23
     (then #(data-source/move-realms))
     (catch (fn [error]
              (log/error "Could not move realms" error)))
     (then #(data-source/open-base-realm encryption-key)))))

(defn change-multiaccount
  [address password encryption-key create-database-if-not-exist?]
  (log/debug "changing multiaccount to: " address)
  (..
   (js/Promise.
    (fn [on-success on-error]
      (try
        (data-source/close-account-realm)
        (on-success)
        (catch :default e
          (on-error {:message (str e)
                     :error   :closing-account-failed})))))
   (then
    (if create-database-if-not-exist?
      #(js/Promise. (fn [on-success] (on-success)))
      #(data-source/db-exists? address)))
   (then
    #(data-source/check-db-encryption address password encryption-key))
   (then
    #(data-source/open-account address password encryption-key))))

(defn merge-events-of-type [success-events event-type]
  ;; merges data value of events of specified type together
  ;; keeps the other events intact
  ;; [[:e1 [:d1]] [:e1 [:d2]]] => [[:e1 [:d1 :d2]]]
  (let [event-to-merge? (fn [event]
                          (and (vector? event)
                               (= (first event) event-type)
                               (vector? (second event))))
        unmergeable-events (filter (complement event-to-merge?) success-events)
        mergeable-events (filter event-to-merge? success-events)]
    (into []
          (into unmergeable-events
                (when-not (empty? mergeable-events)
                  (let [merged-values (reduce into
                                              (map second mergeable-events))]
                    [(into [event-type]
                           (when merged-values
                             [merged-values]))]))))))

(defn- merge-persistence-events [success-events]
  (merge-events-of-type success-events :message/messages-persisted))

(defn- perform-transactions [raw-transactions realm]
  (let [success-events (keep :success-event raw-transactions)
        transactions   (map (fn [{:keys [transaction] :as f}]
                              (or transaction f)) raw-transactions)]
    (data-source/write realm #(doseq [transaction transactions]
                                (transaction realm)))
    (let [optimized-events (merge-persistence-events success-events)]
      (doseq [event optimized-events]
        (re-frame/dispatch event)))))

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

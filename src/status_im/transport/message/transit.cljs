(ns ^{:doc "Transit custom readers and writers, required when adding a new record implementing StatusMessage protocol"}
 status-im.transport.message.transit
  (:require [status-im.transport.message.contact :as contact]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.group-chat :as group-chat]
            [status-im.transport.message.pairing :as pairing]
            [status-im.constants :as constants]
            [cognitect.transit :as transit]))

;; When adding a new reccord implenting the StatusMessage protocol it is required to implement:
;; - a handler that will turn the clojure record into a javascript datastructure.
;; - a reader that will turn the javascript datastructure back into a clojure record.

;; Use the existing types as exemples of how this is done

;;
;; Writer handlers
;;

;; Each writer defines a tag and a representation
;; The tag will determine which reader is used to recreate the clojure record
;; When migrating a particular record, it is important to use a different type and still handle the previous
;; gracefully for compatibility

(deftype ContactRequestHandler []
  Object
  (tag [this v] "c2")
  (rep [this {:keys [name profile-image address]}]
    #js [name profile-image address nil nil]))

(deftype ContactRequestConfirmedHandler []
  Object
  (tag [this v] "c3")
  (rep [this {:keys [name profile-image address]}]
    #js [name profile-image address nil nil]))

(deftype ContactUpdateHandler []
  Object
  (tag [this v] "c6")
  (rep [this {:keys [name profile-image address]}]
    #js [name profile-image address nil nil]))

(deftype MessageHandler []
  Object
  (tag [this v] "c4")
  (rep [this {:keys [content content-type message-type clock-value timestamp]}]
    (condp = content-type
      constants/content-type-text ;; append new content add the end, still pass content the old way at the old index
      #js [(:text content) content-type message-type clock-value timestamp content]
      ;; no need for legacy conversions for rest of the content types
      #js [content content-type message-type clock-value timestamp])))

(deftype GroupMembershipUpdateHandler []
  Object
  (tag [this v] "g5")
  (rep [this {:keys [chat-id membership-updates message]}]
    #js [chat-id membership-updates message]))

(deftype SyncInstallationHandler []
  Object
  (tag [this v] "p1")
  (rep [this {:keys [contacts account chat]}]
    #js [contacts account chat]))

(deftype PairInstallationHandler []
  Object
  (tag [this v] "p2")
  (rep [this {:keys [name installation-id device-type]}]
    #js [installation-id device-type name nil]))

(def writer (transit/writer :json
                            {:handlers
                             {contact/ContactRequest           (ContactRequestHandler.)
                              contact/ContactRequestConfirmed  (ContactRequestConfirmedHandler.)
                              contact/ContactUpdate            (ContactUpdateHandler.)
                              protocol/Message                 (MessageHandler.)
                              group-chat/GroupMembershipUpdate (GroupMembershipUpdateHandler.)
                              pairing/SyncInstallation         (SyncInstallationHandler.)
                              pairing/PairInstallation         (PairInstallationHandler.)}}))

;;
;; Reader handlers
;;

(defn- legacy->new-message-data [content content-type]
  ;; handling only the text content case
  (cond
    (= content-type constants/content-type-text)
    (if (and (map? content) (string? (:text content)))
      ;; correctly formatted map
      [content content-type]
      ;; create safe `{:text string-content}` value from anything else
      [{:text (str content)} content-type])
    :else
    [content content-type]))

;; Here we only need to call the record with the arguments parsed from the clojure datastructures
(def reader (transit/reader :json
                            {:handlers
                             {"c2" (fn [[name profile-image address _ _]]
                                     (contact/ContactRequest. name profile-image address nil nil))
                              "c3" (fn [[name profile-image address _ _]]
                                     (contact/ContactRequestConfirmed. name profile-image address nil nil))
                              "c4" (fn [[legacy-content content-type message-type clock-value timestamp content]]
                                     (let [[new-content new-content-type] (legacy->new-message-data (or content legacy-content) content-type)]
                                       (protocol/Message. new-content new-content-type message-type clock-value timestamp)))
                              "c7" (fn [[content content-type message-type clock-value timestamp]]
                                     (protocol/Message. content content-type message-type clock-value timestamp))
                              "c5" (fn [])
                              "c6" (fn [[name profile-image address _ _]]
                                     (contact/ContactUpdate. name profile-image address nil nil))
                              "g5" (fn [[chat-id membership-updates message]]
                                     (group-chat/GroupMembershipUpdate. chat-id membership-updates message))
                              "p1" (fn [[contacts account chat]]
                                     (pairing/SyncInstallation. contacts account chat))
                              "p2" (fn [[installation-id device-type name _]]
                                     (pairing/PairInstallation. installation-id device-type name nil))}}))

(defn serialize
  "Serializes a record implementing the StatusMessage protocol using the custom writers"
  [o]
  (transit/write writer o))

(defn deserialize
  "Deserializes a record implementing the StatusMessage protocol using the custom readers"
  [o]
  (try (transit/read reader o) (catch :default e nil)))

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

(deftype ^js ContactRequestHandler []
  Object
  (tag [this v] "c2")
  (rep [this {:keys [name profile-image address fcm-token device-info]}]
    #js [name profile-image address fcm-token device-info]))

(deftype ^js ContactRequestConfirmedHandler []
  Object
  (tag [this v] "c3")
  (rep [this {:keys [name profile-image address fcm-token device-info]}]
    #js [name profile-image address fcm-token device-info]))

(deftype ^js ContactUpdateHandler []
  Object
  (tag [this v] "c6")
  (rep [this {:keys [name profile-image address fcm-token device-info]}]
    #js [name profile-image address fcm-token device-info]))

;; It's necessary to support old clients understanding only older, verbose command content (`release/0.9.25` and older)
(defn- new->legacy-command-data [{:keys [command-path params] :as content}]
  (get {["send" #{:personal-chats}]    [{:command-ref ["transactor" :command 83 "send"]
                                         :command "send"
                                         :bot "transactor"
                                         :command-scope-bitmask 83}
                                        constants/content-type-command]
        ["request" #{:personal-chats}] [{:command-ref ["transactor" :command 83 "request"]
                                         :request-command-ref ["transactor" :command 83 "send"]
                                         :command "request"
                                         :request-command "send"
                                         :bot "transactor"
                                         :command-scope-bitmask 83
                                         :prefill [(get params :asset)
                                                   (get params :amount)]}
                                        constants/content-type-command-request]}
       command-path))

(deftype ^js MessageHandler []
  Object
  (tag [this v] "c4")
  (rep [this {:keys [content content-type message-type clock-value timestamp]}]
    (condp = content-type
      constants/content-type-text ;; append new content add the end, still pass content the old way at the old index
      #js [(:text content) content-type message-type clock-value timestamp content]
      constants/content-type-command ;; handle command compatibility issues
      (let [[legacy-content legacy-content-type] (new->legacy-command-data content)]
        #js [(merge content legacy-content) (or legacy-content-type content-type) message-type clock-value timestamp])
      ;; no need for legacy conversions for rest of the content types
      #js [content content-type message-type clock-value timestamp])))

(deftype ^js MessagesSeenHandler []
  Object
  (tag [this v] "c5")
  (rep [this {:keys [message-ids]}]
    (clj->js message-ids)))

(deftype ^js GroupMembershipUpdateHandler []
  Object
  (tag [this v] "g5")
  (rep [this {:keys [chat-id membership-updates message]}]
    #js [chat-id membership-updates message]))

(deftype ^js SyncInstallationHandler []
  Object
  (tag [this v] "p1")
  (rep [this {:keys [contacts account chat]}]
    #js [contacts account chat]))

(deftype ^js PairInstallationHandler []
  Object
  (tag [this v] "p2")
  (rep [this {:keys [name installation-id device-type fcm-token]}]
    #js [installation-id device-type name fcm-token]))

(def writer (transit/writer :json
                            {:handlers
                             {contact/ContactRequest           (ContactRequestHandler.)
                              contact/ContactRequestConfirmed  (ContactRequestConfirmedHandler.)
                              contact/ContactUpdate            (ContactUpdateHandler.)
                              protocol/Message                 (MessageHandler.)
                              protocol/MessagesSeen            (MessagesSeenHandler.)
                              group-chat/GroupMembershipUpdate (GroupMembershipUpdateHandler.)
                              pairing/SyncInstallation         (SyncInstallationHandler.)
                              pairing/PairInstallation         (PairInstallationHandler.)}}))

;;
;; Reader handlers
;;

(def ^:private legacy-ref->new-path
  {["transactor" :command 83 "send"]    ["send" #{:personal-chats}]
   ["transactor" :command 83 "request"] ["request" #{:personal-chats}]})

(defn- legacy->new-command-content [{:keys [command-path command-ref] :as content}]
  (if command-path
    ;; `:command-path` set, message produced by newer app version, nothing to do
    content
    ;; we have to look up `:command-path` based on legacy `:command-ref` value (`release/0.9.25` and older) and assoc it to content
    (assoc content :command-path (get legacy-ref->new-path command-ref))))

(defn- legacy->new-message-data [content content-type]
  ;; handling only the text content case
  (cond
    (= content-type constants/content-type-text)
    (if (and (map? content) (string? (:text content)))
      ;; correctly formatted map
      [content content-type]
      ;; create safe `{:text string-content}` value from anything else
      [{:text (str content)} content-type])
    (or (= content-type constants/content-type-command)
        (= content-type constants/content-type-command-request))
    [(legacy->new-command-content content) constants/content-type-command]
    :else
    [content content-type]))

;; Here we only need to call the record with the arguments parsed from the clojure datastructures
(def reader (transit/reader :json
                            {:handlers
                             {"c2" (fn [[name profile-image address fcm-token device-info]]
                                     (contact/ContactRequest. name profile-image address fcm-token device-info))
                              "c3" (fn [[name profile-image address fcm-token device-info]]
                                     (contact/ContactRequestConfirmed. name profile-image address fcm-token device-info))
                              "c4" (fn [[legacy-content content-type message-type clock-value timestamp content]]
                                     (let [[new-content new-content-type] (legacy->new-message-data (or content legacy-content) content-type)]
                                       (protocol/Message. new-content new-content-type message-type clock-value timestamp)))
                              "c7" (fn [[content content-type message-type clock-value timestamp]]
                                     (protocol/Message. content content-type message-type clock-value timestamp))
                              "c5" (fn [message-ids]
                                     (protocol/MessagesSeen. message-ids))
                              "c6" (fn [[name profile-image address fcm-token device-info]]
                                     (contact/ContactUpdate. name profile-image address fcm-token device-info))
                              "g5" (fn [[chat-id membership-updates message]]
                                     (group-chat/GroupMembershipUpdate. chat-id membership-updates message))
                              "p1" (fn [[contacts account chat]]
                                     (pairing/SyncInstallation. contacts account chat))
                              "p2" (fn [[installation-id device-type name fcm-token]]
                                     (pairing/PairInstallation. installation-id device-type name fcm-token))}}))

(defn serialize
  "Serializes a record implementing the StatusMessage protocol using the custom writers"
  [o]
  (transit/write writer o))

(defn deserialize
  "Deserializes a record implementing the StatusMessage protocol using the custom readers"
  [o]
  (try (transit/read reader o) (catch :default e nil)))

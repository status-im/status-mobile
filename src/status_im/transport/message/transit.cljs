(ns status-im.transport.message.transit
  (:require [status-im.transport.message.v1.contact :as v1.contact]
            [cognitect.transit :as transit]))

(deftype NewContactKeyHandler []
  Object
  (tag [this v] "c1")
  (rep [this {:keys [sym-key message]}]
    #js [sym-key message]))

(deftype ContactRequestHandler []
  Object
  (tag [this v] "c2")
  (rep [this {:keys [name profile-image address fcm-token]}]
    #js [name profile-image address fcm-token]))

(deftype ContactRequestConfirmedHandler []
  Object
  (tag [this v] "c3")
  (rep [this {:keys [name profile-image address fcm-token]}]
    #js [name profile-image address fcm-token]))

(deftype ContactMessageHandler []
  Object
  (tag [this v] "c4")
  (rep [this {:keys [content content-type message-type to-clock-value timestamp]}]
    #js [content content-type message-type to-clock-value timestamp]))

(def reader (transit/reader :json
                            {:handlers
                             {"c1" (fn [[sym-key message]]
                                     (v1.contact/NewContactKey. sym-key message))
                              "c2" (fn [[name profile-image address fcm-token]]
                                     (v1.contact/ContactRequest. name profile-image address fcm-token))
                              "c3" (fn [[name profile-image address fcm-token]]
                                     (v1.contact/ContactRequestConfirmed. name profile-image address fcm-token))
                              "c4" (fn [[content content-type message-type to-clock-value timestamp]]
                                     (v1.contact/ContactMessage. content content-type message-type to-clock-value timestamp))}}))

(def writer (transit/writer :json
                            {:handlers
                             {v1.contact/NewContactKey (NewContactKeyHandler.)
                              v1.contact/ContactRequest (ContactRequestHandler.)
                              v1.contact/ContactRequestConfirmed (ContactRequestConfirmedHandler.)
                              v1.contact/ContactMessage (ContactMessageHandler.)}}))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

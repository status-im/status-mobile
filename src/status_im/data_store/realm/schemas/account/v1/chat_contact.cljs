(ns status-im.data-store.realm.schemas.account.v1.chat-contact
  (:require [taoensso.timbre :as log]))

(def schema {:name       :chat-contact
             :properties {:identity   "string"
                          :is-in-chat {:type    "bool"
                                       :default true}}})

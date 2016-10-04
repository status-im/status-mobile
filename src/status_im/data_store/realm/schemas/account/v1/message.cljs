(ns status-im.data-store.realm.schemas.account.v1.message
  (:require [taoensso.timbre :as log]))

(def schema {:name       :message
             :primaryKey :message-id
             :properties {:message-id     "string"
                          :from           "string"
                          :to             {:type     "string"
                                           :optional true}
                          :group-id       {:type     "string"
                                           :optional true}
                          :content        "string" ;; TODO make it ArrayBuffer
                          :content-type   "string"
                          :timestamp      "int"
                          :chat-id        {:type    "string"
                                           :indexed true}
                          :outgoing       "bool"
                          :retry-count    {:type    :int
                                           :default 0}
                          :same-author    "bool"
                          :same-direction "bool"
                          :preview        {:type     :string
                                           :optional true}
                          :message-type   {:type     :string
                                           :optional true}
                          :message-status {:type     :string
                                           :optional true}
                          :user-statuses  {:type       :list
                                           :objectType "user-status"}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating message schema"))
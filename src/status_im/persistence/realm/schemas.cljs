(ns status-im.persistence.realm.schemas
  (:require [status-im.components.styles :refer [default-chat-color]]))

(def base {:schema        [{:name       :account
                            :primaryKey :address
                            :properties {:address      "string"
                                         :public-key   "string"
                                         :name         {:type "string" :optional true}
                                         :phone        {:type "string" :optional true}
                                         :email        {:type "string" :optional true}
                                         :status       {:type "string" :optional true}
                                         :photo-path   "string"
                                         :last-updated {:type "int" :default 0}}}
                           {:name       :kv-store
                            :primaryKey :key
                            :properties {:key   "string"
                                         :value "string"}}]
           :schemaVersion 0})

(def account {:schema        [{:name       :contact
                               :primaryKey :whisper-identity
                               :properties {:address          {:type "string" :optional true}
                                            :whisper-identity "string"
                                            :name             {:type "string" :optional true}
                                            :photo-path       {:type "string" :optional true}
                                            :last-updated     {:type "int" :default 0}
                                            :last-online      {:type "int" :default 0}}}
                              {:name       :request
                               :properties {:message-id :string
                                            :chat-id    :string
                                            :type       :string
                                            :status     {:type    :string
                                                         :default "open"}
                                            :added      :date}}
                              {:name       :tag
                               :primaryKey :name
                               :properties {:name  "string"
                                            :count {:type "int" :optional true :default 0}}}
                              {:name       :discovery
                               :primaryKey :message-id
                               :properties {:message-id   "string"
                                            :name         {:type "string" :optional true}
                                            :status       "string"
                                            :whisper-id   "string"
                                            :photo-path   {:type "string" :optional true}
                                            :tags         {:type       "list"
                                                           :objectType "tag"}
                                            :priority     {:type "int" :default 0}
                                            :last-updated "date"}}
                              {:name       :kv-store
                               :primaryKey :key
                               :properties {:key   "string"
                                            :value "string"}}
                              {:name       :message
                               :primaryKey :message-id
                               :properties {:message-id      "string"
                                            :from            "string"
                                            :to              {:type     "string"
                                                              :optional true}
                                            :group-id        {:type     "string"
                                                              :optional true}
                                            :content         "string" ;; TODO make it ArrayBuffer
                                            :content-type    "string"
                                            :timestamp       "int"
                                            :chat-id         {:type    "string"
                                                              :indexed true}
                                            :outgoing        "bool"
                                            :delivery-status {:type     "string"
                                                              :optional true}
                                            :retry-count     {:type    :int
                                                              :default 0}
                                            :same-author     "bool"
                                            :same-direction  "bool"
                                            :preview         {:type     :string
                                                              :optional true}
                                            :message-type    {:type     :string
                                                              :optional true}}}
                              {:name       :pending-message
                               :primaryKey :message-id
                               :properties {:message-id  "string"
                                            :chat-id     {:type     "string"
                                                          :optional true}
                                            :message     "string"
                                            :timestamp   "int"
                                            :status      "string"
                                            :retry-count "int"
                                            :send-once   "bool"
                                            :identities  {:type     "string"
                                                          :optional true}
                                            :internal?   {:type     "bool"
                                                          :optional true}}}
                              {:name       :chat-contact
                               :properties {:identity   "string"
                                            :is-in-chat {:type    "bool"
                                                         :default true}}}
                              {:name       :chat
                               :primaryKey :chat-id
                               :properties {:chat-id         "string"
                                            :name            "string"
                                            :color           {:type    "string"
                                                              :default default-chat-color}
                                            :group-chat      {:type    "bool"
                                                              :indexed true}
                                            :is-active       "bool"
                                            :timestamp       "int"
                                            :contacts        {:type       "list"
                                                              :objectType "chat-contact"}
                                            :dapp-url        {:type     :string
                                                              :optional true}
                                            :dapp-hash       {:type     :int
                                                              :optional true}
                                            :last-message-id "string"}}
                              {:name       :command
                               :primaryKey :chat-id
                               :properties {:chat-id "string"
                                            :file    "string"}}]
              :schemaVersion 0})


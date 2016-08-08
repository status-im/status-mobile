(ns status-im.persistence.realm.schemas
  (:require [status-im.components.styles :refer [default-chat-color]]))

(def base {:schema        [{:name       :accounts
                            :primaryKey :address
                            :properties {:address    "string"
                                         :public-key "string"
                                         :name       "string"
                                         :phone      {:type "string" :optional true}
                                         :email      {:type "string" :optional true}
                                         :status     {:type "string" :optional true}
                                         :photo-path "string"}}
                           {:name       :tag
                            :primaryKey :name
                            :properties {:name  "string"
                                         :count {:type     "int"
                                                 :optional true
                                                 :default  0}}}
                           {:name       :discoveries
                            :primaryKey :whisper-id
                            :properties {:name         "string"
                                         :status       "string"
                                         :whisper-id   "string"
                                         :photo        "string"
                                         :location     "string"
                                         :tags         {:type       "list"
                                                        :objectType "tag"}
                                         :last-updated "date"}}
                           {:name       :kv-store
                            :primaryKey :key
                            :properties {:key   "string"
                                         :value "string"}}]
           :schemaVersion 0})

(def account {:schema [{:name       :contacts
                        :primaryKey :whisper-identity
                        :properties {:phone-number     {:type     "string"
                                                        :optional true}
                                     :whisper-identity "string"
                                     :name             {:type     "string"
                                                        :optional true}
                                     :photo-path       {:type    "string"
                                                        :optinal true}}}
                       {:name       :requests
                        :properties {:message-id :string
                                     :chat-id    :string
                                     :type       :string
                                     :status     {:type    :string
                                                  :default "open"}
                                     :added      :date}}
                       {:name       :kv-store
                        :primaryKey :key
                        :properties {:key   "string"
                                     :value "string"}}
                       {:name       :msgs
                        :primaryKey :msg-id
                        :properties {:msg-id          "string"
                                     :from            "string"
                                     :to              {:type     "string"
                                                       :optional true}
                                     :content         "string" ;; TODO make it ArrayBuffer
                                     :content-type    "string"
                                     :timestamp       "int"
                                     :chat-id         {:type    "string"
                                                       :indexed true}
                                     :outgoing        "bool"
                                     :delivery-status {:type     "string"
                                                       :optional true}
                                     :same-author     "bool"
                                     :same-direction  "bool"
                                     :preview         {:type     :string
                                                       :optional true}}}
                       {:name       :chat-contact
                        :properties {:identity   "string"
                                     :is-in-chat {:type    "bool"
                                                  :default true}}}
                       {:name       :chats
                        :primaryKey :chat-id
                        :properties {:chat-id     "string"
                                     :name        "string"
                                     :color       {:type    "string"
                                                   :default default-chat-color}
                                     :group-chat  {:type    "bool"
                                                   :indexed true}
                                     :is-active   "bool"
                                     :timestamp   "int"
                                     :contacts    {:type       "list"
                                                   :objectType "chat-contact"}
                                     :dapp-url    {:type     :string
                                                   :optional true}
                                     :dapp-hash   {:type     :int
                                                   :optional true}
                                     :last-msg-id "string"}}
                       {:name       :commands
                        :primaryKey :chat-id
                        :properties {:chat-id "string"
                                     :file    "string"}}]
              :schemaVersion 0})


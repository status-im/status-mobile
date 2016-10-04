(ns status-im.data-store.realm.schemas.account.v1.chat
  (:require [taoensso.timbre :as log]
            [status-im.components.styles :refer [default-chat-color]]))

(def schema {:name       :chat
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
                          :removed-at      {:type     :int
                                            :optional true}
                          :last-message-id "string"
                          :public-key      {:type     :string
                                            :optional true}
                          :private-key     {:type     :string
                                            :optional true}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating chat schema"))
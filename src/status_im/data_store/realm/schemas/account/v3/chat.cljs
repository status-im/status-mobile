(ns status-im.data-store.realm.schemas.account.v3.chat
  (:require [status-im.ui.components.styles :refer [default-chat-color]]))

(def schema {:name       :chat
             :primaryKey :chat-id
             :properties {:chat-id                :string
                          :name                   :string
                          :color                  {:type    :string
                                                   :default default-chat-color}
                          :group-chat             {:type    :bool
                                                   :indexed true}
                          :group-admin            {:type     :string
                                                   :optional true}
                          :is-active              :bool
                          :timestamp              :int
                          :contacts               {:type     "string[]"}
                          :removed-at             {:type     :int
                                                   :optional true}
                          :removed-from-at        {:type     :int
                                                   :optional true}
                          :deleted-at-clock-value {:type :int
                                                   :optional true}
                          :added-to-at            {:type     :int
                                                   :optional true}
                          :updated-at             {:type     :int
                                                   :optional true}
                          :message-overhead       {:type    :int
                                                   :default 0}
                          :contact-info           {:type     :string
                                                   :optional true}
                          :debug?                 {:type    :bool
                                                   :default false}
                          :public?                {:type    :bool
                                                   :default false}}})

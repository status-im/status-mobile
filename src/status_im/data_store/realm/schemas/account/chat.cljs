(ns status-im.data-store.realm.schemas.account.chat
  (:require [status-im.ui.components.styles :refer [default-chat-color]]))

(def v1 {:name       :chat
         :primaryKey :chat-id
         :properties {:chat-id          :string
                      :name             :string
                      :color            {:type    :string
                                         :default default-chat-color}
                      :group-chat       {:type    :bool
                                         :indexed true}
                      :group-admin      {:type     :string
                                         :optional true}
                      :is-active        :bool
                      :timestamp        :int
                      :contacts         {:type     "string[]"}
                      :removed-at       {:type     :int
                                         :optional true}
                      :removed-from-at  {:type     :int
                                         :optional true}
                      :added-to-at      {:type     :int
                                         :optional true}
                      :updated-at       {:type     :int
                                         :optional true}
                      :message-overhead {:type    :int
                                         :default 0}
                      :contact-info     {:type     :string
                                         :optional true}
                      :debug?           {:type    :bool
                                         :default false}
                      :public?          {:type    :bool
                                         :default false}}})

(def v3 {:name       :chat
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

(def v5 {:name       :chat
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
                      :debug?                 {:type    :bool
                                               :default false}
                      :public?                {:type    :bool
                                               :default false}}})

(def v6 {:name       :chat
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
                      :membership-version     {:type :int
                                               :optional true}
                      :membership-signature   {:type :string
                                               :optional true}
                      :debug?                 {:type    :bool
                                               :default false}
                      :public?                {:type    :bool
                                               :default false}}})

(def v7 {:name       :chat
         :primaryKey :chat-id
         :properties {:chat-id                :string
                      :name                   :string
                      :color                  {:type    :string
                                               :default default-chat-color}
                      :group-chat             {:type    :bool
                                               :indexed true}
                      :is-active              :bool
                      :timestamp              :int
                      :contacts               {:type     "string[]"}
                      :admins                 {:type     "string[]"}
                      :membership-updates    {:type       :list
                                              :objectType :membership-update}
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
                      :debug?                 {:type    :bool
                                               :default false}
                      :public?                {:type    :bool
                                               :default false}}})

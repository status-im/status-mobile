(ns status-im.data-store.realm.schemas.account.chat
  (:require [status-im.ui.components.colors :as colors]))

(def v1 {:name       :chat
         :primaryKey :chat-id
         :properties {:chat-id          :string
                      :name             :string
                      :color            {:type    :string
                                         :default colors/default-chat-color}
                      :group-chat       {:type    :bool
                                         :indexed true}
                      :group-admin      {:type     :string
                                         :optional true}
                      :is-active        :bool
                      :timestamp        :int
                      :contacts         {:type "string[]"}
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
                                               :default colors/default-chat-color}
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
                                               :default colors/default-chat-color}
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
                                               :default colors/default-chat-color}
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
                                               :default colors/default-chat-color}
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

(def v8 {:name       :chat
         :primaryKey :chat-id
         :properties {:chat-id                :string
                      :name                   :string
                      :color                  {:type    :string
                                               :default colors/default-chat-color}
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
                                               :default false}
                      :tags                   {:type     "string[]"}}})

(def v9 {:name       :chat
         :primaryKey :chat-id
         :properties {:chat-id                 :string
                      :name                    :string
                      :color                   {:type    :string
                                                :default colors/default-chat-color}
                      :group-chat              {:type    :bool
                                                :indexed true}
                      :is-active               :bool
                      :timestamp               :int
                      :contacts                {:type "string[]"}
                      :admins                  {:type "string[]"}
                      :membership-updates      {:type       :list
                                                :objectType :membership-update}
                      :removed-at              {:type     :int
                                                :optional true}
                      :removed-from-at         {:type     :int
                                                :optional true}
                      :deleted-at-clock-value  {:type     :int
                                                :optional true}
                      :added-to-at             {:type     :int
                                                :optional true}
                      :updated-at              {:type     :int
                                                :optional true}
                      :message-overhead        {:type    :int
                                                :default 0}
                      :debug?                  {:type    :bool
                                                :default false}
                      :public?                 {:type    :bool
                                                :default false}
                      :tags                    {:type "string[]"}
                      :unviewed-messages-count {:type    :int
                                                :default 0}}})

(def v10
  (update v9 :properties merge
          {:last-message-content {:type     :string
                                  :optional true}
           :last-message-type    {:type     :string
                                  :optional true}}))

(def v11
  (update v10 :properties merge
          {:last-clock-value {:type     :int
                              :optional true}}))
(def v12
  (-> v11
      (update :properties merge
              {:last-message-content-type
               {:type     :string
                :optional true}})
      (update :properties dissoc :last-message-type)))

(def v13
  (update v12 :properties assoc
          :members-joined         {:type "string[]"}))

(def v14
  (update v13 :properties assoc
          :group-chat-local-version {:type :int
                                     :optional true}))
(def v15
  (update v14 :properties dissoc
          :message-overhead
          :removed-from-at
          :added-to-at
          :removed-at))

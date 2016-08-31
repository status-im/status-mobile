(ns status-im.persistence.realm.schemas
  (:require [status-im.components.styles :refer [default-chat-color]]))

(def base {:schema        [{:name       :account
                            :primaryKey :address
                            :properties {:address      :string
                                         :public-key   :string
                                         :name         {:type :string :optional true}
                                         :phone        {:type :string :optional true}
                                         :email        {:type :string :optional true}
                                         :status       {:type :string :optional true}
                                         :photo-path   :string
                                         :last-updated {:type :int :default 0}}}
                           {:name       :kv-store
                            :primaryKey :key
                            :properties {:key   :string
                                         :value :string}}]
           :schemaVersion 0})

(def chat-contact
  {:properties {:identity   :string
                :is-in-chat {:type     :bool
                             :default  true
                             :optional true}
                :added-at   :int}})

(def request
  {:properties {:message-id :string
                :chat-id    :string
                :type       :string
                :status     {:type    :string
                             :default "open"}
                :added      :date}})

(def discovery
  {:primaryKey :message-id
   :properties {:message-id   :string
                :name         {:type     :string
                               :optional true}
                :status       :string
                :whisper-id   :string
                :photo-path   {:type     :string
                               :optional true}
                :tags         {:type       :list
                               :objectType :tag}
                :priority     {:type    :int
                               :default 0}
                :last-updated "date"}})

(def kv-store
  {:primaryKey :key :properties {:key   :string
                                 :value :string}})

(def pending-message
  {:primaryKey :message-id
   :properties {:message-id  :string
                :send-once   :bool
                :internal?   {:type     :bool
                              :optional true}
                :status      :string
                :chat-id     {:type     :string
                              :optional true}
                :timestamp   :int
                :retry-count :int
                :identities  {:type     :string
                              :optional true}
                :message     :string}})

(def command
  {:primaryKey :chat-id
   :properties {:chat-id :string
                :file    :string}})

(def chat
  {:primaryKey :chat-id
   :properties {:last-message-id :string
                :color           {:type    :string
                                  :default default-chat-color}
                :contacts        {:type       :list
                                  :objectType :chat-contact}
                :name            :string
                :dapp-url        {:type     :string
                                  :optional true}
                :dapp-hash       {:type     :int
                                  :optional true}
                :is-active       {:type    :bool
                                  :default true}
                :group-chat      {:type    :bool
                                  :indexed true}
                :removed-at      {:type     :int
                                  :optional true}
                :chat-id         :string
                :timestamp       :int}})

(def tag
  {:primaryKey :name
   :properties {:name  :string
                :count {:type     :int
                        :optional true
                        :default  0}}})

(def contact
  {:primaryKey :whisper-identity
   :properties {:address          {:type     :string
                                   :optional true}
                :whisper-identity :string
                :name             {:type     :string
                                   :optional true}
                :photo-path       {:type     :string
                                   :optional true}
                :last-updated     {:type    :int
                                   :default 0}
                :last-online      {:type    :int
                                   :default 0}}})

(def message
  {:primaryKey :message-id
   :properties {:same-author     :bool
                :message-id      :string
                :group-id        {:type     :string
                                  :optional true}
                :content         :string
                :same-direction  :bool
                :message-type    {:type     :string
                                  :optional true}
                :delivery-status {:type     :string
                                  :optional true}
                :preview         {:type     :string
                                  :optional true}
                :from            :string
                :chat-id         {:type    :string
                                  :indexed true}
                :content-type    :string
                :timestamp       :int
                :retry-count     {:type    :int
                                  :default 0}
                :outgoing        :bool
                :to              {:type     :string
                                  :optional true}}})

(def account-schema
  {:chat-contact    chat-contact
   :request         request
   :discovery       discovery
   :kv-store        kv-store
   :pending-message pending-message
   :command         command
   :chat            chat
   :tag             tag
   :contact         contact
   :message         message})

(defn make-schema [schema]
  (mapv (fn [[n coll]]
          (assoc coll :name n)) schema))

(def default-schema
  (make-schema account-schema))

(def account
  {:schemaVersion 1
   :schema        default-schema})


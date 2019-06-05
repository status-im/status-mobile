(ns status-im.data-store.realm.schemas.account.transport)

(def v1 {:name       :transport
         :primaryKey :chat-id
         :properties {:chat-id          :string
                      :ack              :string
                      :seen             :string
                      :pending-ack      :string
                      :pending-send     :string
                      :topic            :string
                      :sym-key-id       {:type :string
                                         :optional true}
                      ;;TODO (yenda) remove once go implements persistence
                      :sym-key          {:type :string
                                         :optional true}}})

(def v4 {:name       :transport
         :primaryKey :chat-id
         :properties {:chat-id          :string
                      :ack              :string
                      :seen             :string
                      :pending-ack      :string
                      :pending-send     :string
                      :topic            :string
                      :fetch-history?   {:type :bool
                                         :default false}
                      :sym-key-id       {:type :string
                                         :optional true}
                      ;;TODO (yenda) remove once go implements persistence
                      :sym-key          {:type :string
                                         :optional true}}})

(def v6 {:name       :transport
         :primaryKey :chat-id
         :properties {:chat-id               :string
                      :ack                   :string
                      :seen                  :string
                      :pending-ack           :string
                      :pending-send          :string
                      :topic                 :string
                      :fetch-history?        {:type    :bool
                                              :default false}
                      :resend?               {:type     :string
                                              :optional true}
                      :sym-key-id            {:type    :string
                                              :optional true}
                      ;;TODO (yenda) remove once go implements persistence
                      :sym-key               {:type     :string
                                              :optional true}}})

(def v7 {:name       :transport
         :primaryKey :chat-id
         :properties {:chat-id               :string
                      :ack                   :string
                      :seen                  :string
                      :pending-ack           :string
                      :pending-send          :string
                      :topic                 {:type     :string
                                              :optional true}
                      :resend?               {:type     :string
                                              :optional true}
                      :sym-key-id            {:type    :string
                                              :optional true}
                      ;;TODO (yenda) remove once go implements persistence
                      :sym-key               {:type     :string
                                              :optional true}}})

(def v8 (assoc-in v7 [:properties :one-to-one]
                  {:type     :bool
                   :optional true}))

(def v9 (update v8 :properties
                dissoc
                :ack :seen :pending-ack :pending-send))

(def v10 (update v9 :properties dissoc :one-to-one :topic :sym-key-id :sym-key))

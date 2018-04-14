(ns status-im.data-store.realm.schemas.account.v1.transport)

(def schema {:name       :transport
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

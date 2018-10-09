(ns status-im.data-store.realm.schemas.account.transport-inbox-topic)

(def v1 {:name       :transport-inbox-topic
         :primaryKey :topic
         :properties {:topic        :string
                      :last-request {:type :int :default 1}}})

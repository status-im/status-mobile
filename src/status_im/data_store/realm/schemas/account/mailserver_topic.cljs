(ns status-im.data-store.realm.schemas.account.mailserver-topic)

(def v1 {:name       :mailserver-topic
         :primaryKey :topic
         :properties {:topic        :string
                      :chat-ids     :string
                      :last-request {:type :int :default 1}}})

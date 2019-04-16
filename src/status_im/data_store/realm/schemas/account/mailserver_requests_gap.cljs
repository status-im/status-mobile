(ns status-im.data-store.realm.schemas.account.mailserver-requests-gap)

(def v1 {:name       :mailserver-requests-gap
         :primaryKey :id
         :properties {:id      :string
                      :chat-id {:type    :string
                                :indexed true}
                      :from    {:type    :int
                                :indexed true}
                      :to      :int}})

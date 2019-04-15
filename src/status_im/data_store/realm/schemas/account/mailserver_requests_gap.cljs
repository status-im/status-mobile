(ns status-im.data-store.realm.schemas.account.mailserver-requests-gap)

(def v1 {:name       :mailserver-requests-gap
         :properties {:chat-id {:type    :bool
                                :indexed true}
                      :from    {:type    :integer
                                :indexed true}
                      :to      :integer}})

(ns status-im.data-store.realm.schemas.account.chat-requests-range)

(def v1 {:name       :chat-requests-range
         :primaryKey :chat-id
         :properties {:chat-id :string
                      :lowest-request-from {:type     :int
                                            :optional true}
                      :highest-request-to {:type     :int
                                           :optional true}}})

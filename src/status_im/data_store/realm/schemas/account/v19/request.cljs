(ns status-im.data-store.realm.schemas.account.v19.request)

(def schema {:name       :request
             :properties {:message-id :string
                          :chat-id    :string
                          :response   :string
                          :status     {:type    :string
                                       :default "open"}}})

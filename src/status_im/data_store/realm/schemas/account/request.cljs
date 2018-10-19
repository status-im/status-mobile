(ns status-im.data-store.realm.schemas.account.request)

(def v1 {:name       :request
         :properties {:message-id :string
                      :chat-id    :string
                      :response   :string
                      :status     {:type    :string
                                   :default "open"}}})

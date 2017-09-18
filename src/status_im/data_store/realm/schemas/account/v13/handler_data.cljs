
(ns status-im.data-store.realm.schemas.account.v13.handler-data)

(def schema {:name       :handler-data
             :primaryKey :message-id
             :properties {:message-id "string"
                          :data    {:type    "string"
                                    :default "{}"}}})

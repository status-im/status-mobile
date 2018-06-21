(ns status-im.data-store.realm.schemas.account.v8.browser)

(def schema {:name       :browser
             :primaryKey :browser-id
             :properties {:browser-id :string
                          :name       :string
                          :timestamp  :int
                          :dapp?      {:type    :bool
                                       :default false}
                          :url        {:type     :string
                                       :optional true}
                          :contact    {:type     :string
                                       :optional true}
                          :history-index  {:type :int
                                           :optional true}
                          :history    {:type     :vector
                                       :optional true}}})
